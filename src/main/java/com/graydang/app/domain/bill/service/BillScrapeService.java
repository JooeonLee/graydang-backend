package com.graydang.app.domain.bill.service;

import com.graydang.app.domain.bill.repository.BillScrapeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillScrapeService {

    private final BillScrapeRepository billScrapeRepository;

    public long getBillScrapeCountByUserId(Long userId) {
        return billScrapeRepository.countByUserId(userId);
    }
}
