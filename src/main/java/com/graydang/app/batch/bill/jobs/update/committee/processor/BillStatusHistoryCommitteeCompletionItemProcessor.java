package com.graydang.app.batch.bill.jobs.update.committee.processor;

import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillCommissionResponseDto;
import com.graydang.app.batch.bill.jobs.update.committee.dto.BillStatusHistoryCommitteeUpdateDto;
import com.graydang.app.domain.bill.model.Bill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillStatusHistoryCommitteeCompletionItemProcessor implements ItemProcessor<Bill, BillStatusHistoryCommitteeUpdateDto> {

    private final BillApiClient billApiClient;

    // 멀티스레드 환경에서도 안전하게 숫자를 세는 카운터
    private final AtomicLong asyncFilterCount = new AtomicLong(0);

    // API 스로틀링 로직
    private static final int SLEEP_MS = 50;

    /**
     * Reader가 읽은 'Bill' 객체를 받아 API 호출 및 데이터 가공을 수행합니다.
     *
     * @param bill Reader가 읽어온 'bill' 테이블 엔티티
     * @return '소관위 회부' 정보가 확인되면 Writer로 넘길 DTO, 없으면 null (필터링)
     */
    @Override
    public BillStatusHistoryCommitteeUpdateDto process(Bill bill) throws Exception {

        // billId 방어 로직
        if (bill.getBillId() == null) {
            asyncFilterCount.incrementAndGet();
            return null;
        }

        try {
            // Tasklet의 Thread.sleep() 로직
            // 멀티스레드 환경에서도 각 스레드(Processor)가 API 호출 속도를 조절합니다.
            // Thread.sleep(SLEEP_MS);

            // Tasklet의 API 호출 로직 (bill.getBillId() 사용)
            Optional<BillCommissionResponseDto> responseOpt = billApiClient.getBillCommissionInfo(bill.getBillId());

            if (responseOpt.isPresent()) {
                BillCommissionResponseDto.BodyDto body = responseOpt.get().getBody();
                if (body != null) {
                    List<BillCommissionResponseDto.JurisdictionExaminationItem> items = body.getJurisdictionExamination();

                    if (items != null && !items.isEmpty()) {
                        BillCommissionResponseDto.JurisdictionExaminationItem item = items.get(0);

                        // Job 2는 '처리 결과(procResultCd)'가 있어야만 통과시킵니다.
                        String procResult = item.getProcResultCd();

                        if (procResult != null && !procResult.isBlank()) {
                            // 처리 결과가 확인된 건만 Writer로 전달 (UPDATE 대상)
                            return new BillStatusHistoryCommitteeUpdateDto(bill.getId(), bill.getBillId(), item);
                        } else {
                            // 아직 '심사 중'인 경우 -> 필터링 (null 반환)
                            // (Writer로 넘어가지 않고 FilterCount가 증가함)
                            asyncFilterCount.incrementAndGet();
                            return null;
                        }
                    }
                }
            }
            // API를 호출했으나 '소관위 회부' 정보가 없는 경우 (아직 발의 상태)
            // null을 반환하여 Writer로 전달되지 않도록 필터링합니다.
            asyncFilterCount.incrementAndGet();
            return null;

        //} catch (InterruptedException ie) {
            // 멀티스레드 중단 시
        //    log.warn("Processor Interrupted - billId: {}", bill.getBillId());
        //    Thread.currentThread().interrupt(); // 인터럽트 상태 복원
        //    asyncFilterCount.incrementAndGet();
        //    return null;
        } catch (Exception e) {
            // (8) Tasklet의 예외 처리 로직 (API 호출 오류 등)
            log.warn("❌ API 처리 실패 - billId: {}. 이 항목을 Skip합니다. Error: {}",
                    bill.getBillId(), e.getMessage());
            // 실패한 항목은 null을 반환하여 이번 배치에서는 제외 (다음 배치에서 재시도)
            asyncFilterCount.incrementAndGet();
            return null;
        }
    }

    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        stepExecution.setFilterCount(stepExecution.getFilterCount() + asyncFilterCount.get());
        asyncFilterCount.set(0);
    }
}
