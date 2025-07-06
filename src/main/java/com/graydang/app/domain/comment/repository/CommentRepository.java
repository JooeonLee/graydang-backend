package com.graydang.app.domain.comment.repository;

import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.comment.model.Comment;
import com.graydang.app.domain.comment.model.dto.CommentSimpleResponseDto;
import com.graydang.app.domain.comment.repository.projection.CommentSimpleProjection;
import com.graydang.app.domain.comment.repository.projection.MyCommentProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    long countByBill(Bill bill);

    long countByUserIdAndStatus(Long userId, String status);

    @EntityGraph(attributePaths = {"user.profile"})
    Slice<Comment> findByBillIdAndStatusOrderByCreatedAtDesc(Long billId, String status, Pageable pageable);

    @Query("SELECT c.id FROM Comment c WHERE c.user.id = :userId AND c.status = :status")
    Set<Long> findCommentIdsByUserIdAndStatus(Long userId, String status);

    Optional<Comment> findByIdAndStatus(Long commentId, String status);


    @Query("""
        SELECT
            b.id AS billId,
            c.id AS commentId,
            c.content AS content,
            c.createdAt AS createdAt,
            COUNT(l) AS likeCount,
            b.aiTitle AS title
        FROM Comment c
        LEFT JOIN c.likes l ON c.id = l.comment.id AND l.status = 'ACTIVE'
        JOIN Bill b ON c.bill.id = b.id
        WHERE c.user.id = :userId AND c.status = 'ACTIVE'
        GROUP BY c.id, c.content, c.createdAt, b.aiTitle
        ORDER BY c.createdAt DESC
    """)
    Slice<MyCommentProjection> findMyCommentsByUserId(Long userId, Pageable pageable);
}
