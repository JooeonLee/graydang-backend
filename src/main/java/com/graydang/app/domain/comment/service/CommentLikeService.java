package com.graydang.app.domain.comment.service;

import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.service.BillService;
import com.graydang.app.domain.comment.model.Comment;
import com.graydang.app.domain.comment.model.CommentLike;
import com.graydang.app.domain.comment.repository.CommentLikeRepository;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final UserService userService;
    private final CommentService commentService;

    @Transactional
    public boolean toggleLike(Long userId, Long commentId) {
        User user = userService.findByIdOrThrow(userId);
        Comment comment = commentService.findByIdAndStatusOrThrow(commentId);

        Optional<CommentLike> commentLikeOptional = commentLikeRepository.findByUserIdAndCommentId(user.getId(), comment.getId());

        if (commentLikeOptional.isPresent()) {
            CommentLike commentLike = commentLikeOptional.get();

            if(commentLike.getStatus().equals("DELETED")) {
                commentLike.restore();
                return true;
            }
            else {
                commentLike.softDelete();
                return false;
            }
        }

        CommentLike commentLike = CommentLike.builder()
                .user(user)
                .comment(comment)
                .status("ACTIVE")
                .build();
        commentLikeRepository.save(commentLike);
        return true;
    }
}
