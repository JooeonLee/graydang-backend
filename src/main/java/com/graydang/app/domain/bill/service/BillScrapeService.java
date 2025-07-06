package com.graydang.app.domain.bill.service;

import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.BillScrape;
import com.graydang.app.domain.bill.model.dto.BillSimpleResponseDto;
import com.graydang.app.domain.bill.repository.BillScrapeRepository;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.service.UserService;
import com.graydang.app.global.common.model.dto.SliceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillScrapeService {

    private final BillScrapeRepository billScrapeRepository;
    private final UserService userService;

    public long getBillScrapeCountByUserId(Long userId) {
        return billScrapeRepository.countByUserIdAndStatus(userId, "ACTIVE");
    }

    public SliceResponse<BillSimpleResponseDto> getScrapedBillsByUserId(Long userId, Pageable pageable) {
        int limit = pageable.getPageSize() + 1;
        int offset = (int) pageable.getOffset();

        List<Object[]> raw = billScrapeRepository
                .findScrapedBillsByUserId(userId, limit, offset);

        boolean hasNext = raw.size() > pageable.getPageSize();
        List<BillSimpleResponseDto> content = raw.stream()
                .limit(pageable.getPageSize())
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
                .toList();

        Slice<BillSimpleResponseDto> slice = new SliceImpl<>(content, pageable, hasNext);

        return new SliceResponse<>(slice);
    }

    public long getBillScrapeCountByBillId(Long billId) {
        return billScrapeRepository.countByBillIdAndStatus(billId, "ACTIVE");
    }

    @Transactional
    public boolean toggleScrape(Long userId, Bill bill) {
        User user = userService.findByIdOrThrow(userId);

        Optional<BillScrape> billScrapeOptional =  billScrapeRepository.findByUserIdAndBillId(userId, bill.getId());

        if(billScrapeOptional.isPresent()) {
            BillScrape billScrape = billScrapeOptional.get();

            if(billScrape.getStatus().equals("DELETED")) {
                billScrape.restore();
                return true;
            }
            else {
                billScrape.softDelete();
                return false;
            }
        }

        BillScrape billScrape = BillScrape.builder()
                .user(user)
                .bill(bill)
                .status("ACTIVE")
                .build();
        billScrapeRepository.save(billScrape);
        return true;
    }
}
