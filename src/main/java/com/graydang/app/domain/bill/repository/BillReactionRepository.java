package com.graydang.app.domain.bill.repository;

import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.BillReaction;
import com.graydang.app.domain.bill.repository.projection.BillReactionCountProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BillReactionRepository extends JpaRepository<BillReaction, Long> {

    long countByBill(Bill bill);

    long countByBillId(Long billId);

    long countByUserId(Long userId);

    @EntityGraph(attributePaths = {"bill"})
    Slice<BillReaction> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    Optional<BillReaction> findByUserIdAndBillId(Long userId, Long billId);

    @Query("""
    SELECT r.reactionType AS reactionType, COUNT(r) AS count
    FROM BillReaction r
    WHERE r.bill.id = :billId AND r.status = 'ACTIVE'
    GROUP BY r.reactionType
    """)
    List<BillReactionCountProjection> countReactionsByBillId(@Param("billId") Long billId);
}
