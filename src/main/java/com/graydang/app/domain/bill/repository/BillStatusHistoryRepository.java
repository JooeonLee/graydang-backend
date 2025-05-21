package com.graydang.app.domain.bill.repository;

import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.BillStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillStatusHistoryRepository extends JpaRepository<BillStatusHistory, Long> {

    Optional<BillStatusHistory> findByBillAndStepName(Bill bill, String stepName);
}
