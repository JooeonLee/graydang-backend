package com.graydang.app.domain.bill.repository;

import com.graydang.app.domain.bill.model.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
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

    @Query(value = """
    SELECT
        b.id AS billId,        
        b.ai_title AS aiTitle,
        b.representative_name AS representativeName,
        DATE_FORMAT(b.propose_date, '%Y.%m.%d') AS proposeDate,
        IFNULL(
            (
                SELECT h.step_name
                FROM bill_status_history h
                WHERE h.bill_id = b.id AND h.status = 'ACTIVE'
                ORDER BY h.step_order DESC
                LIMIT 1
            ),
            "발의"
        ) AS billHistoryStatus,
        b.committee_name AS committeeName,
        b.view_count AS viewCount,
        (
            SELECT COUNT(*)
            FROM bill_reaction r
            WHERE r.bill_id = b.id AND r.status = 'ACTIVE'
        ) AS reactionCount,
        (
            SELECT COUNT(*)
            FROM comment c
            WHERE c.bill_id = b.id AND c.status = 'ACTIVE'
        ) AS commentCount,
        FALSE AS scraped
    FROM bill b
    WHERE b.status = 'ACTIVE'
    ORDER BY b.view_count DESC
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Object[]> findPopularBills(@Param("limit") Integer limit, @Param("offset") Integer offset);

    @Query(value = """
    SELECT
        b.id AS billId,        
        b.ai_title AS aiTitle,
        b.representative_name AS representativeName,
        DATE_FORMAT(b.propose_date, '%Y.%m.%d') AS proposeDate,
        IFNULL(
            (
                SELECT h.step_name
                FROM bill_status_history h
                WHERE h.bill_id = b.id AND h.status = 'ACTIVE'
                ORDER BY h.step_order DESC
                LIMIT 1
            ),
            "발의"
        ) AS billHistoryStatus,
        b.committee_name AS committeeName,
        b.view_count AS viewCount,
        (
            SELECT COUNT(*)
            FROM bill_reaction r
            WHERE r.bill_id = b.id AND r.status = 'ACTIVE'
        ) AS reactionCount,
        (
            SELECT COUNT(*)
            FROM comment c
            WHERE c.bill_id = b.id AND c.status = 'ACTIVE'
        ) AS commentCount,
        EXISTS (
            SELECT 1
            FROM bill_scrape s
            WHERE s.bill_id = b.id AND s.user_id = :userId AND s.status = 'ACTIVE'
        ) AS scraped
    FROM bill b
    WHERE b.status = 'ACTIVE'
    ORDER BY b.view_count DESC
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Object[]> findPopularBillsWithScraped(
            @Param("userId") Long userId,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    Boolean existsByBillId(@NonNull String billId);
}
