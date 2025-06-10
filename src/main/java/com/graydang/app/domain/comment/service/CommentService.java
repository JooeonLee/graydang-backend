package com.graydang.app.domain.comment.service;

import com.graydang.app.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public long getCommentCountByUserId(Long userId) {
        return commentRepository.countByUserId(userId);
    }
}
