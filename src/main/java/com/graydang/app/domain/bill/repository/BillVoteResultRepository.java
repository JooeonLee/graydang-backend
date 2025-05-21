package com.graydang.app.domain.bill.repository;

import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.BillVoteResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillVoteResultRepository extends JpaRepository<BillVoteResult, Long> {

    Optional<BillVoteResult> findByBill(Bill bill);
}
