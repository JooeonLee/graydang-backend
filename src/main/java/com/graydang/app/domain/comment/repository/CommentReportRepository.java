package com.graydang.app.domain.comment.repository;

import com.graydang.app.domain.comment.model.CommentReport;
import com.graydang.app.domain.comment.repository.projection.ReportedCommentSummaryProjection;
import com.graydang.app.domain.comment.repository.projection.UserReportHistoryProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {

    Boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    @Query("""
        SELECT 
            c.id AS commentId,
            c.content AS commentContent,
            b.id AS billId,
            b.aiTitle AS billTitle,
            b.proposeDate AS proposeDate,
            u.id AS commenterUserId,
            u.email AS commenterEmail,
            up.nickname AS commenterNickname,
            COUNT(r) AS reportedCount
        FROM CommentReport r
        JOIN r.comment c
        JOIN c.bill b
        JOIN c.user u
        JOIN u.profile up
        GROUP BY c.id, c.content, b.id, b.title, b.proposeDate, u.id, u.username, up.nickname
        ORDER BY MAX(r.createdAt) DESC
    """
    )
    Slice<ReportedCommentSummaryProjection> findReportedComments(Pageable pageable);
    
    @Query("""
        SELECT 
            r.id AS reportId,
            r.reason AS reportReason,
            r.status AS reportStatus,
            r.createdAt AS reportedAt,
            c.id AS commentId,
            c.content AS commentContent,
            cu.id AS commenterId,
            cu.email AS commenterEmail,
            cup.nickname AS commenterNickname,
            b.id AS billId,
            b.aiTitle AS billTitle,
            b.proposeDate AS billProposeDate
        FROM CommentReport r
        JOIN r.comment c
        JOIN c.user cu
        JOIN cu.profile cup
        JOIN c.bill b
        WHERE r.user.id = :userId
        ORDER BY r.createdAt DESC
    """
    )
    Slice<UserReportHistoryProjection> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("""
        SELECT 
            r.id AS reportId,
            r.reason AS reportReason,
            r.status AS reportStatus,
            r.createdAt AS reportedAt,
            c.id AS commentId,
            c.content AS commentContent,
            ru.id AS reporterId,
            ru.email AS reporterEmail,
            rup.nickname AS reporterNickname,
            b.id AS billId,
            b.aiTitle AS billTitle,
            b.proposeDate AS billProposeDate
        FROM CommentReport r
        JOIN r.comment c
        JOIN r.user ru
        JOIN ru.profile rup
        JOIN c.bill b
        WHERE r.reportedUser.id = :reportedUserId
        ORDER BY r.createdAt DESC
    """
    )
    Slice<UserReportHistoryProjection> findByReportedUserId(@Param("reportedUserId") Long reportedUserId, Pageable pageable);
}
