package com.graydang.app.domain.comment.repository;

import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    long countByBill(Bill bill);

    long countByUserId(Long userId);
}
