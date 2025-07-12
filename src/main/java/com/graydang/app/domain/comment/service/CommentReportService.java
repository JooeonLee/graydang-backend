package com.graydang.app.domain.comment.service;

import com.graydang.app.domain.comment.exception.CommentReportException;
import com.graydang.app.domain.comment.model.Comment;
import com.graydang.app.domain.comment.model.CommentReport;
import com.graydang.app.domain.comment.model.dto.CommentReportSaveRequestDto;
import com.graydang.app.domain.comment.repository.CommentReportRepository;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.service.UserService;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentReportService {

    private final CommentReportRepository commentReportRepository;
    private final UserService userService;
    private final CommentService commentService;

    @Transactional
    public long reportComment(CommentReportSaveRequestDto requestDto, Long userId, Long commentId) {

        User user = userService.findByIdOrThrow(userId);
        Comment comment = commentService.findByIdAndStatusOrThrow(commentId);

        if(commentReportRepository.existsByUserIdAndCommentId(user.getId(), comment.getId())) {
            throw new CommentReportException(BaseResponseStatus.ALREADY_REPORTED);
        }

        CommentReport commentReport = requestDto.toEntity(user, comment);
        CommentReport savedCommentReport = commentReportRepository.save(commentReport);

        return savedCommentReport.getId();
    }
}
