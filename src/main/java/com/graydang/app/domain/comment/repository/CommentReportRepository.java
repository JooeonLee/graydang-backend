package com.graydang.app.domain.comment.repository;

import com.graydang.app.domain.comment.model.CommentReport;
import com.graydang.app.domain.comment.repository.projection.ReportedCommentSummaryProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
