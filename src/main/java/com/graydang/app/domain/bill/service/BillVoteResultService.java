package com.graydang.app.domain.bill.service;

import com.graydang.app.batch.bill.dto.BillVoteResultResponseDto;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.BillVoteResult;
import com.graydang.app.domain.bill.repository.BillRepository;
import com.graydang.app.domain.bill.repository.BillVoteResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillVoteResultService {

    private final BillRepository billRepository;
    private final BillVoteResultRepository billVoteResultRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String STATUS_ACTIVE = "ACTIVE";

    @Transactional
    public void saveVoteResult(String billId, BillVoteResultResponseDto.Item item) {
        if (item == null) {
            log.warn("표결 결과 item이 null 입니다. - billId: {}", billId);
            return;
        }

        LocalDate voteDate = parseDate(item.getProcDate());

        if (voteDate == null || item.getProcResultCd() == null || item.getProcResultCd().isBlank()) {
            log.info("표결 처리 정보가 없어 저장 생략 - billId: {}", billId);
            return;
        }

        Bill bill = billRepository.findByBillId(billId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 billId를 찾을 수 없습니다: " + billId));

        Optional<BillVoteResult> existingOpt = billVoteResultRepository.findByBill(bill);

        if (existingOpt.isPresent()) {
            BillVoteResult existing = existingOpt.get();
            existing.update(
                    voteDate,
                    item.getYesCount(),
                    item.getNoCount(),
                    item.getAbstentionCount(),
                    item.getMemberTotalCount(),
                    item.getVoteTotalCount(),
                    item.getProcResultCd(),
                    STATUS_ACTIVE
            );
            log.info("🔁 표결 결과 업데이트 - billId: {}, 찬성: {}, 반대: {}", billId, item.getYesCount(), item.getNoCount());
        } else {
            BillVoteResult result = BillVoteResult.builder()
                    .bill(bill)
                    .voteDate(voteDate)
                    .agreeCount(item.getYesCount())
                    .opposeCount(item.getNoCount())
                    .abstentionCount(item.getAbstentionCount())
                    .absenceCount(item.getMemberTotalCount() - item.getVoteTotalCount()) // 계산 방식 명확히 조율 가능
                    .totalCount(item.getVoteTotalCount())
                    .voteResult(item.getProcResultCd())
                    .status(STATUS_ACTIVE)
                    .build();

            billVoteResultRepository.save(result);
            log.info("✅ 표결 결과 저장 완료 - billId: {}, 찬성: {}", billId, item.getYesCount());
        }
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        return LocalDate.parse(date, DATE_FORMATTER);
    }
}