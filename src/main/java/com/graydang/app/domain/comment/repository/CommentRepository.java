package com.graydang.app.domain.comment.repository;

import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.comment.model.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    long countByBill(Bill bill);

    long countByUserId(Long userId);

    @EntityGraph(attributePaths = {"user.profile"})
    Slice<Comment> findByBillIdAndStatusOrderByCreatedAtDesc(Long billId, String status, Pageable pageable);

    @Query("SELECT c.id FROM Comment c WHERE c.user.id = :userId AND c.status = :status")
    Set<Long> findCommentIdsByUserIdAndStatus(Long userId, String status);

    Optional<Comment> findByIdAndStatus(Long commentId, String status);
}
