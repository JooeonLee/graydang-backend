package com.graydang.app.domain.bill.repository;

import com.graydang.app.domain.bill.model.BillScrape;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillScrapeRepository extends JpaRepository<BillScrape, Long> {

    boolean existsByUserIdAndBillId(Long userId, Long billId);

    long countByUserId(Long userId);
}
