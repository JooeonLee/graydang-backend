package com.graydang.app.batch.bill.jobs.committee.processor;

import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillCommissionResponseDto;
import com.graydang.app.batch.bill.jobs.committee.dto.BillUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillCommitteeItemProcessor implements ItemProcessor<String, BillUpdateDto> {

    private final BillApiClient billApiClient;
    private static final int SLEEP_MS = 50;

    @Override
    public BillUpdateDto process(String billId) throws Exception {
        log.info("Processing billId: {}", billId);
        Thread.sleep(SLEEP_MS);

        try {
            return billApiClient.getBillCommissionInfo(billId)
                    .flatMap(BillCommissionResponseDto::getFirstJurisdictionExaminationItem)
                    .map(item -> new BillUpdateDto(billId, item))
                    .orElse(null);
        } catch (Exception e) {
            log.warn("❌ API 호출 실패 - billId: {}", billId, e);
            return null; // 실패 시 null을 반환하여 Writer로 넘어가지 않도록 함
        }
    }
}
