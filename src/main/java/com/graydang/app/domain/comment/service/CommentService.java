package com.graydang.app.domain.comment.service;

import com.graydang.app.domain.comment.exception.CommentException;
import com.graydang.app.domain.comment.model.Comment;
import com.graydang.app.domain.comment.model.dto.CommentSaveRequestDto;
import com.graydang.app.domain.comment.repository.CommentRepository;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public long getCommentCountByUserId(Long userId) {
        return commentRepository.countByUserId(userId);
    }

    @Transactional
    public long updateComment(Long commentId, Long userId, CommentSaveRequestDto requestDto) {
        Comment comment = findByIdAndStatusOrThrow(commentId);

        if(!comment.getUser().getId().equals(userId)) {
            throw new CommentException(BaseResponseStatus.UNAUTHORIZED_COMMENT_ACCESS);
        }
        comment.updateContent(requestDto.content());

        return commentId;
    }

    @Transactional
    public long deleteComment(Long commentId, Long userId) {
        Comment comment = findByIdAndStatusOrThrow(commentId);
        if(!comment.getUser().getId().equals(userId)) {
            throw new CommentException(BaseResponseStatus.UNAUTHORIZED_COMMENT_ACCESS);
        }
        comment.softDelete();

        return commentId;
    }

    public Comment findByIdAndStatusOrThrow(Long commentId) {
        return commentRepository.findByIdAndStatus(commentId, "ACTIVE")
                .orElseThrow(() -> new CommentException(BaseResponseStatus.NON_COMMENT));
    }
}
