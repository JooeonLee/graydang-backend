package com.graydang.app.domain.bill.repository;

import com.graydang.app.domain.bill.model.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByBillId(String billId);

    @Query(value = "SELECT b.billId FROM Bill b ")
    List<String> findAllBillIds(Pageable pageable);

    @Query("SELECT b.billId FROM Bill b ORDER BY b.proposeDate DESC")
    Page<String> findLatestBillIds(Pageable pageable);

    Page<Bill> findByAiProcessedFalse(PageRequest pageRequest);

    Optional<Bill> findFirstByAiProcessedFalseOrderByIdAsc();
}
