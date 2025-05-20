package com.graydang.app.domain.repository;

import com.graydang.app.domain.bill.Bill;
import com.graydang.app.domain.bill.BillStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillStatusHistoryRepository extends JpaRepository<BillStatusHistory, Long> {

    Optional<BillStatusHistory> findByBillAndStepName(Bill bill, String stepName);
}
