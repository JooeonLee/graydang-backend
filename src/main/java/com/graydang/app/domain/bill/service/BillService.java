package com.graydang.app.domain.bill.service;

import com.graydang.app.batch.bill.dto.BillInfoResponseDto;
import com.graydang.app.domain.bill.exception.BillException;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.BillStatusHistory;
import com.graydang.app.domain.bill.model.dto.BillDetailResponseDto;
import com.graydang.app.domain.bill.model.dto.BillStatusHistoryResponseDto;
import com.graydang.app.domain.bill.repository.BillReactionRepository;
import com.graydang.app.domain.bill.repository.BillRepository;
import com.graydang.app.domain.bill.repository.BillStatusHistoryRepository;
import com.graydang.app.domain.comment.repository.CommentRepository;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BillService {

    private final BillRepository billRepository;
    private final BillReactionRepository billReactionRepository;
    private final BillStatusHistoryRepository billStatusHistoryRepository;
    private final CommentRepository commentRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

    @Transactional
    public void saveOrUpdate(BillInfoResponseDto.ItemDto dto) {
        Optional<Bill> existing = billRepository.findByBillId(dto.getBillId());

        if (existing.isPresent()) {
            update(existing.get(), dto);
        } else {
            Bill bill = toEntity(dto);
            billRepository.save(bill);
        }
    }

    private Bill toEntity(BillInfoResponseDto.ItemDto dto) {
        return Bill.builder()
                .billId(dto.getBillId())
                .title(dto.getBillName())
                .proposeDate(parseDate(dto.getProposeDt()))
                .processResult(dto.getGeneralResult())
                .billStatus(dto.getProcStageCd())
                .summary(dto.getSummary())
                .representativeName(parseRepresentativeName(dto.getProposerKind()))
                .status("ACTIVE")
                .build();
    }

    private void update(Bill bill, BillInfoResponseDto.ItemDto dto) {
        bill.update(
                dto.getBillName(),
                parseDate(dto.getProposeDt()),
                null, // committeeName: 현재 DTO에는 없음
                dto.getGeneralResult(),
                dto.getProcStageCd(),
                null, // summary: 현재 DTO에는 없음
                parseRepresentativeName(dto.getProposerKind())
        );
    }

    private String parseRepresentativeName(String proposerKind) {
        if (proposerKind == null || proposerKind.isBlank()) return null;
        return proposerKind.replace("의원", "").trim();
    }

    private LocalDate parseDate(String yyyymmdd) {
        try {
            return LocalDate.parse(yyyymmdd); // yyyy-MM-dd 형식으로 전달된다고 가정
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void updateCommitteeName(String billId, String committeeName) {
        Optional<Bill> optional = billRepository.findByBillId(billId);
        optional.ifPresent(bill -> bill.updateCommitteeName(committeeName));
    }

    public BillDetailResponseDto getBillDetail(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new BillException(BaseResponseStatus.NONE_BILL));

        long reactionCount = billReactionRepository.countByBill(bill);
        long commentCount = commentRepository.countByBill(bill);

        List<BillStatusHistoryResponseDto> history = new ArrayList<>(
                billStatusHistoryRepository.findByBillOrderByStepOrderAsc(bill)
                        .stream()
                        .map(BillStatusHistoryResponseDto::from)
                        .toList()
        );

        history.add(BillStatusHistoryResponseDto.buildZeroOrder(bill.getId(), bill.getProposeDate().toString()));
        history.sort(Comparator.comparing(BillStatusHistoryResponseDto::getStepOrder));

        return BillDetailResponseDto.of(bill, reactionCount, commentCount, history);
    }
}
