package com.graydang.app.domain.comment.repository;

import com.graydang.app.domain.comment.model.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);


    @Query("""
        SELECT cl.comment.id
        FROM CommentLike cl
        WHERE cl.user.id = :userId
        AND cl.status = :status
    """)
    Set<Long> findLikedCommentIdsByUserIdAndStatus(Long userId, String status);
}
