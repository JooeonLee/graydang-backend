package com.graydang.app.batch.bill.jobs.update.committee.writer;

import com.graydang.app.batch.bill.dto.BillCommissionResponseDto;
import com.graydang.app.batch.bill.jobs.update.committee.dto.BillStatusHistoryCommitteeUpdateDto;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.BillStatusHistory;
import com.graydang.app.domain.bill.repository.BillRepository;
import com.graydang.app.domain.bill.service.BillStatusHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillStatusHistoryCommitteeCompletionItemWriter implements ItemWriter<BillStatusHistoryCommitteeUpdateDto> {

    private final BillRepository billRepository;
    private final BillStatusHistoryService billStatusHistoryService;

    @Override
    public void write(Chunk<? extends BillStatusHistoryCommitteeUpdateDto> chunk) throws Exception {

        for (BillStatusHistoryCommitteeUpdateDto dto : chunk.getItems()) {
            BillCommissionResponseDto.JurisdictionExaminationItem item = dto.getCommitteeItem();

            Bill bill = billRepository.findById(dto.getId()).orElse(null);
            if (bill == null) {
                log.warn("🔥 [WRITE_SKIP] Bill 엔티티 없음. PK: {}", dto.getId());
                continue;
            }

            // Service의 업데이트 메서드 호출
            billStatusHistoryService.updateCommitteeExaminationResult(bill, item);
        }
    }
}
