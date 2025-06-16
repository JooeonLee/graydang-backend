package com.graydang.app.domain.bill.repository;

import com.graydang.app.domain.bill.model.BillScrape;
import com.graydang.app.domain.bill.model.dto.BillSimpleResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillScrapeRepository extends JpaRepository<BillScrape, Long> {

    boolean existsByUserIdAndBillId(Long userId, Long billId);

    long countByBillId(Long billId);

    long countByUserId(Long userId);

    List<BillScrape> findByUserId(Long userId);

    @Query(value = """
        SELECT
            b.ai_title AS aiTitle,
            b.representative_name AS representativeName,
            DATE_FORMAT(b.propose_date, '%Y.%m.%d') AS proposeDate,
            (
                SELECT h.step_name
                FROM bill_status_history h
                WHERE h.bill_id = b.id AND h.status = 'ACTIVE'
                ORDER BY h.step_order DESC
                LIMIT 1
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
            TRUE AS scraped
        FROM bill_scrape s
        JOIN bill b ON s.bill_id = b.id
        WHERE s.user_id = :userId
          AND s.status = 'ACTIVE'
          AND b.status = 'ACTIVE'
        ORDER BY s.created_at DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Object[]> findScrapedBillsByUserId(
            @Param("userId") Long userId,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset);

}
