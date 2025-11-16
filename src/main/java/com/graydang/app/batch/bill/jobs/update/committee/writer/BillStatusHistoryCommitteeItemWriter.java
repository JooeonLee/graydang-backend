package com.graydang.app.batch.bill.jobs.update.committee.writer;

import com.graydang.app.batch.bill.dto.BillCommissionResponseDto;
import com.graydang.app.batch.bill.jobs.update.committee.dto.BillStatusHistoryCommitteeUpdateDto;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.BillStatusHistory;
import com.graydang.app.domain.bill.repository.BillRepository;
import com.graydang.app.domain.bill.repository.BillStatusHistoryRepository;
import com.graydang.app.domain.bill.service.BillService;
import com.graydang.app.domain.bill.service.BillStatusHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component // (1) 스프링 빈으로 등록
@RequiredArgsConstructor
public class BillStatusHistoryCommitteeItemWriter implements ItemWriter<BillStatusHistoryCommitteeUpdateDto> {

    private final BillRepository billRepository;
    private final BillService billService;
    private final BillStatusHistoryService historyService;

    /**
     * Processor가 전달한 DTO 청크를 받아 DB에 일괄 저장합니다.
     *
     * @param chunk Processor에서 전달된 BillStatusHistoryCommitteeUpdateDto의 묶음
     */
    @Override
    public void write(Chunk<? extends BillStatusHistoryCommitteeUpdateDto> chunk) throws Exception {

        for (BillStatusHistoryCommitteeUpdateDto dto : chunk.getItems()) {
            BillCommissionResponseDto.JurisdictionExaminationItem item = dto.getCommitteeItem();

            // Writer에서 스킵 기준(Guard Clause) 추가
            String submitDt = item.getSubmitDt();
            if (submitDt == null || submitDt.isBlank()) {
                log.info("⚠️ [WRITE_SKIP] '회부일'(submitDt)이 없어 저장을 생략합니다. billId: {}", dto.getBillId());
                continue; // 이 아이템은 스킵하고 다음 DTO로 넘어감
            }

            // DTO의 PK(billId)로 Bill 엔티티를 조회
            Bill bill = billRepository.findById(dto.getId()).orElse(null);

            if (bill == null) {
                log.warn("🔥 [WRITE_SKIP] Bill 엔티티를 찾을 수 없습니다. PK: {}", dto.getId());
                continue; // 이 아이템은 스킵
            }

            // Bill 엔티티 업데이트
            billService.updateCommitteeName(bill, dto.getCommitteeItem().getCommitteeName());

            // BillStatusHistory 엔티티 신규 생성
            historyService.saveNewCommitteeReferral(bill, item);
        }
    }
}
