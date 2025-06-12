package com.graydang.app.domain.bill.repository;

import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.BillReaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillReactionRepository extends JpaRepository<BillReaction, Long> {

    long countByBill(Bill bill);

    long countByBillId(Long billId);

    long countByUserId(Long userId);


}
