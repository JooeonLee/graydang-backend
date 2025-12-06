package com.graydang.app.batch.bill.jobs.update.transfer.processor;

import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillTransferredInfoResponseDto;
import com.graydang.app.batch.bill.jobs.update.transfer.dto.BillStatusHistoryGovTransferUpdateDto;
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
public class BillStatusHistoryGovTransferItemProcessor implements ItemProcessor<Bill, BillStatusHistoryGovTransferUpdateDto> {

    private final BillApiClient billApiClient;

    // 멀티스레드 환경에서도 안전하게 숫자를 세는 카운터
    private final AtomicLong asyncFilterCount = new AtomicLong(0);

    /**
     * Reader가 읽은 'Bill' 객체를 받아 API 호출 및 데이터 가공을 수행합니다.
     *
     * @param bill Reader가 읽어온 'bill' 테이블 엔티티
     * @return '정부 이송' 정보가 확인되면 Writer로 넘길 DTO, 없으면 null (필터링)
     */
    @Override
    public BillStatusHistoryGovTransferUpdateDto process(Bill bill) throws Exception {
        try {
            // 1. API 호출
            Optional<BillTransferredInfoResponseDto> responseOpt = billApiClient.getBillTransferredInfo(bill.getBillId());

            // 2. 검증 로직
            if (shouldSkip(responseOpt)) {
                asyncFilterCount.incrementAndGet();
                return null;
            }

            // 3. 유효한 데이터 처리
            BillTransferredInfoResponseDto.TransferredItem item = responseOpt.get().getBody().getItems().get(0);
            return new BillStatusHistoryGovTransferUpdateDto(bill.getId(), bill.getBillId(), item);
        } catch (Exception e) {
            // 4. 에러 처리 전략
            log.warn("❌ API 처리 실패 - billId: {}. 이 항목을 Skip합니다. Error: {}",
                    bill.getBillId(), e.getMessage());
            // 실패한 항목은 null을 반환하여 이번 배치에서는 제외 (다음 배치에서 재시도)
            asyncFilterCount.incrementAndGet();
            return null;
        }
    }

    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        long finalFilterCount = asyncFilterCount.get();

        // 1. FilterCount 증가
        stepExecution.setFilterCount(stepExecution.getFilterCount() + asyncFilterCount.get());

        // 2. WriteCount 감소 (WriteCount = 기존 WriteCount - 카운트한 FilterCount)
        // 설명: AsyncWriter가 Future를 받아 일단 Write로 셌던 것들 중, 실제로는 빈 상자였던 개수만큼 뺀다.
        stepExecution.setWriteCount(stepExecution.getWriteCount() - finalFilterCount);

        asyncFilterCount.set(0);
    }

    // 검증 로직 분리
    private boolean shouldSkip(Optional<BillTransferredInfoResponseDto> responseOpt) {
        if (responseOpt.isEmpty()) return true;

        var items = responseOpt.get().getBody().getItems();
        if (items == null || items.isEmpty()) return true;

        var item = items.get(0);
        return item.getTransDt() == null || item.getTransDt().isBlank();
    }
}
