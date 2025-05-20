package com.graydang.app.domain.repository;

import com.graydang.app.domain.bill.Bill;
import com.graydang.app.domain.bill.BillVoteResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillVoteResultRepository extends JpaRepository<BillVoteResult, Long> {

    Optional<BillVoteResult> findByBill(Bill bill);
}
