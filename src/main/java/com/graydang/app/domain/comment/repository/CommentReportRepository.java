package com.graydang.app.domain.comment.repository;

import com.graydang.app.domain.comment.model.CommentReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {

    Boolean existsByUserIdAndCommentId(Long userId, Long commentId);
}
