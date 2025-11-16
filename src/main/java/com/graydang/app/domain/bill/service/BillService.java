package com.graydang.app.domain.bill.service;

import com.graydang.app.batch.bill.dto.BillInfoResponseDto;
import com.graydang.app.batch.bill.dto.BillSaveRequestDto;
import com.graydang.app.domain.bill.exception.BillException;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.BillStatusHistory;
import com.graydang.app.domain.bill.model.dto.BillDetailResponseDto;
import com.graydang.app.domain.bill.model.dto.BillSimpleResponseDto;
import com.graydang.app.domain.bill.model.dto.BillStatusHistoryResponseDto;
import com.graydang.app.domain.bill.repository.*;
import com.graydang.app.domain.bill.repository.projection.BillSimpleProjection;
import com.graydang.app.domain.comment.repository.CommentRepository;
import com.graydang.app.domain.user.model.InterestKeyword;
import com.graydang.app.global.common.model.dto.SliceResponse;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class BillService {

    private final BillRepository billRepository;
    private final BillReactionRepository billReactionRepository;
    private final BillStatusHistoryRepository billStatusHistoryRepository;
    private final BillScrapeRepository billScrapeRepository;
    private final CommentRepository commentRepository;
    private final BillQueryRepository billQueryRepository;

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

    @Transactional
    public void saveOrUpdate(BillSaveRequestDto  requestDto) {
        Optional<Bill> existing = billRepository.findByBillId(requestDto.getBillId());

        if (existing.isPresent()) {
            update(existing.get(), requestDto);
        } else {
            Bill bill = toEntity(requestDto);
            billRepository.save(bill);
        }
    }

    @Transactional
    public void saveAll(List<? extends BillSaveRequestDto> requests) {
        if (requests ==  null || requests.isEmpty()) {
            log.warn("Empty or null requests were provided");
            return;
        }
        for (BillSaveRequestDto requestDto : requests) {
            saveOrUpdate(requestDto);
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

    private  Bill toEntity(BillSaveRequestDto dto) {
        return Bill.builder()
                .billId(dto.getBillId())
                .title(dto.getBillName())
                .proposeDate(parseDate(dto.getProposeDate()))
                .processResult(dto.getProcessResult())
                .billStatus(dto.getBillStatus())
                .summary(dto.getSummary())
                .representativeName(dto.getRepresentativeName())
                .committeeName(dto.getCommitteeName())
                .viewCount(0L)
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

    private void update(Bill bill, BillSaveRequestDto dto) {
        bill.update(
                dto.getBillName(),
                parseDate(dto.getProposeDate()),
                dto.getCommitteeName(),
                dto.getProcessResult(),
                dto.getBillStatus(),
                dto.getSummary(),
                dto.getRepresentativeName()
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

    // ItemWriter에서 활용할 메서드
    @Transactional
    public void updateCommitteeName(Bill bill, String committeeName) {
        bill.updateCommitteeName(committeeName);
    }

    public BillDetailResponseDto getBillDetail(Long id, CustomUserDetails userDetails) {
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

        boolean isScrapped = false;
        if (userDetails != null) {
            Long memberId = userDetails.getId();
            isScrapped = billScrapeRepository.existsByUserIdAndBillId(memberId, id);
        }

        return BillDetailResponseDto.of(bill, reactionCount, commentCount, isScrapped, history);
    }

    public BillSimpleResponseDto getBillSimple(Long id, CustomUserDetails userDetails) {
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
        String billHistoryStatus = history.stream()
                .skip(history.size() - 1)
                .map(BillStatusHistoryResponseDto::getStepName)
                .findFirst()
                .orElse(null);

        boolean isScrapped = false;
        if (userDetails != null) {
            Long memberId = userDetails.getId();
            isScrapped = billScrapeRepository.existsByUserIdAndBillId(memberId, id);
        }

        return  BillSimpleResponseDto.of(bill, billHistoryStatus, reactionCount, commentCount, isScrapped);
    }

    public SliceResponse<BillSimpleResponseDto> getPopularBills(CustomUserDetails userDetails, Pageable pageable) {

        int limit = pageable.getPageSize() + 1;
        int offset = (int)pageable.getOffset();
        List<Object[]> raw;

        if (userDetails == null) {
            raw = billRepository.findPopularBills(limit, offset);
        }
        else {
            Long userId = userDetails.getId();
            raw = billRepository.findPopularBillsWithScraped(userId, limit, offset);
        }

        boolean hasNext = raw.size() > pageable.getPageSize();
        List<BillSimpleResponseDto> content = raw.stream()
                .map(r -> new BillSimpleResponseDto(
                        ((Number) r[0]).longValue(), // billId
                        (String) r[1], // aiTitle
                        (String) r[2], // representativeName
                        (String) r[3], // proposeDate
                        (String) r[4], // billHistoryStatus
                        (String) r[5], // committeeName
                        ((Number) r[6]).longValue(), // viewCount
                        ((Number) r[7]).longValue(), // reactionCount
                        ((Number) r[8]).longValue(), // commentCount
                        ((Number) r[9]).intValue() == 1 // scraped
                ))
                .limit(pageable.getPageSize())
                .toList();

        Slice<BillSimpleResponseDto> slice = new SliceImpl<>(content,  pageable, hasNext);

        return new SliceResponse<>(slice);
    }

    @Transactional
    public void increaseViewCount(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new BillException(BaseResponseStatus.NONE_BILL));

        bill.increaseViewCount();
    }

    public Bill findByIdOrThrow(Long id) {
        return billRepository.findById(id).orElseThrow(() -> new BillException(BaseResponseStatus.NONE_BILL));
    }

    public SliceResponse<BillSimpleResponseDto> getBillsByKeywordLabels(CustomUserDetails userDetails, Set<String> keywords, Pageable pageable, String sortBy) {

        Long userId = userDetails != null ? userDetails.getUser().getId() : null;

        Set<String> committeeLabels = InterestKeyword.convertLabelsToCommitteeLabels(keywords);
        //Set<String> committeeLabels = InterestKeyword.convertNamesToCommitteeLabels(keywords);

        Slice<BillSimpleResponseDto> slice = billQueryRepository.findBillSimpleProjectionByCommittees(
                committeeLabels, userId, pageable, sortBy
        );

        //List<BillSimpleResponseDto> content = slice.getContent().stream()
        //        .map(BillSimpleResponseDto::from)
        //        .toList();

        //return new SliceResponse<>(content, slice.getNumber(), slice.isLast());
        return new SliceResponse<>(slice);
    }
}
