package com.graydang.app.batch.bill.tasklet;

import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillInfoResponseDto;
import com.graydang.app.batch.bill.dto.BillPassageResult;
import com.graydang.app.batch.bill.dto.BillRecentPassageResponseDto;
import com.graydang.app.batch.bill.dto.BillSaveRequestDto;
import com.graydang.app.domain.bill.service.BillService;
import com.graydang.app.monitoring.SlackBotNotifier;
import com.graydang.app.monitoring.SlackPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class TodayPassedBillTasklet implements Tasklet {

    private final BillApiClient billApiClient;
    private final BillService billService;
    private final SlackBotNotifier slackBotNotifier;



    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        log.info("📦 [오늘 통과 법안 수집 배치] 시작");

        int successCount = 0;
        int failCount = 0;
        StringBuilder billSummary = new StringBuilder();
        BillPassageResult result = null;

        try {
            // getRecentPassageList는 전체 조회 건수와 오늘 날짜 필터링된 법안 정보를 반환
            result = billApiClient.getRecentPassageList();
            
            if (result.getTodayPassedBills().isEmpty()) {
                log.info("✔️ 수집 종료: 오늘 통과된 법안이 없습니다.");
                sendSlackNotificationWithStats(true, result.getTotalFetchedCount(), 
                        result.getTodayPassedCount(), 0, 0, null, "오늘 통과된 법안이 없습니다.");
                return RepeatStatus.FINISHED;
            }

            // 각 통과 법안을 BillSaveRequestDto로 변환하여 저장
            for (BillRecentPassageResponseDto.ItemDto item : result.getTodayPassedBills()) {
                try {
                    BillSaveRequestDto saveRequest = BillSaveRequestDto.builder()
                            .billId(item.getBillId())
                            .billName(item.getBillName())
                            .committeeName(item.getCommitteeName())
                            .processResult(item.getGeneralResult())
                            .billStatus(item.getGeneralResult())
                            .proposeDate(item.getProposeDt())
                            .representativeName(item.getProposerKind())
                            .status("ACTIVE")
                            .build();
                    
                    billService.saveOrUpdate(saveRequest);
                    successCount++;
                    
                    // 주요 법안 정보를 요약에 추가 (최대 5개만 표시)
                    if (successCount <= 5) {
                        billSummary.append(String.format("• %s (%s)\n", 
                            item.getBillName(), 
                            item.getGeneralResult()));
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("법안 저장 실패 - billId: {}, error: {}", item.getBillId(), e.getMessage());
                }
            }

            log.info("✅ [오늘 통과 법안 수집 배치] 완료 - 전체 조회: {}건, 오늘 통과: {}건, 성공: {}건, 실패: {}건", 
                    result.getTotalFetchedCount(), result.getTodayPassedCount(), successCount, failCount);
            
            // 성공 시 Slack 알림
            sendSlackNotificationWithStats(true, result.getTotalFetchedCount(), 
                    result.getTodayPassedCount(), successCount, failCount, billSummary.toString(), null);
            
        } catch (Exception e) {
            log.error("❌ [오늘 통과 법안 수집 배치] 실패", e);
            
            // 실패 시 Slack 알림
            int totalFetched = result != null ? result.getTotalFetchedCount() : 0;
            int todayCount = result != null ? result.getTodayPassedCount() : 0;
            sendSlackNotificationWithStats(false, totalFetched, todayCount, 
                    successCount, failCount, null, e.getMessage());
            
            throw new RuntimeException("오늘 통과 법안 수집 실패", e);
        }

        return RepeatStatus.FINISHED;
    }

    private void sendSlackNotificationWithStats(boolean isSuccess, int totalFetchedCount, 
                                                int todayPassedCount, int successCount, int failCount, 
                                                String billSummary, String errorMessage) {
        try {
            String emoji = isSuccess ? "✅" : "🚨";
            String status = isSuccess ? "성공" : "실패";
            
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append(String.format("%s *오늘 통과 법안 수집 배치 %s*\n", emoji, status));
            messageBuilder.append(String.format("• 실행 시간: %s\n", 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            
            // 통계 정보 추가
            messageBuilder.append("\n*📊 수집 통계:*\n");
            messageBuilder.append(String.format("• API 전체 조회: %d건\n", totalFetchedCount));
            messageBuilder.append(String.format("• 오늘 통과 법안: %d건\n", todayPassedCount));
            
            if (isSuccess) {
                if (todayPassedCount > 0) {
                    messageBuilder.append(String.format("• 처리 결과: 성공 %d건, 실패 %d건\n", successCount, failCount));
                    
                    if (billSummary != null && !billSummary.isEmpty()) {
                        messageBuilder.append("\n*📋 주요 통과 법안:*\n");
                        messageBuilder.append(billSummary);
                        if (successCount > 5) {
                            messageBuilder.append(String.format("... 외 %d건\n", successCount - 5));
                        }
                    }
                }
            } else {
                messageBuilder.append(String.format("• 오류 메시지: %s\n", errorMessage));
                if (successCount > 0) {
                    messageBuilder.append(String.format("• 부분 처리: %d건 성공\n", successCount));
                }
            }

            SlackPayload payload = new SlackPayload(messageBuilder.toString(), "#monitoring", null);
            slackBotNotifier.send(payload);
            
        } catch (Exception e) {
            log.error("Slack 알림 발송 실패", e);
            // Slack 알림 실패는 배치 실행에 영향을 주지 않도록 함
        }
    }
}
