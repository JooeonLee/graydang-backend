package com.graydang.app.batch.bill.tasklet;

import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillInfoResponseDto;
import com.graydang.app.batch.bill.dto.BillRecentPassageResponseDto;
import com.graydang.app.batch.bill.dto.BillSaveRequestDto;
import com.graydang.app.domain.bill.service.BillService;
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



    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        log.info("📦 [오늘 통과 법안 수집 배치] 시작");

        try {
            // getRecentPassageList는 오늘 날짜에 통과된 법안만 반환 (고정: pageNo=1, numOfRows=100)
            List<BillRecentPassageResponseDto.ItemDto> todayPassedBills = billApiClient.getRecentPassageList();
            
            if (todayPassedBills.isEmpty()) {
                log.info("✔️ 수집 종료: 오늘 통과된 법안이 없습니다.");
                return RepeatStatus.FINISHED;
            }

            // 각 통과 법안을 BillSaveRequestDto로 변환하여 저장
            for (BillRecentPassageResponseDto.ItemDto item : todayPassedBills) {
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
            }

            log.info("✅ [오늘 통과 법안 수집 배치] 완료 - 총 {}건 수집됨", todayPassedBills.size());
            
        } catch (Exception e) {
            log.error("❌ [오늘 통과 법안 수집 배치] 실패", e);
            throw new RuntimeException("오늘 통과 법안 수집 실패", e);
        }

        return RepeatStatus.FINISHED;
    }
}
