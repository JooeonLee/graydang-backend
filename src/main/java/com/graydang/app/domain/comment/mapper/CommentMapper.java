package com.graydang.app.domain.comment.mapper;

import com.graydang.app.domain.comment.model.Comment;
import com.graydang.app.domain.comment.model.dto.CommentResponseDto;
import com.graydang.app.global.common.util.DateUtil;

public class CommentMapper {

    public static CommentResponseDto toCommentResponseDto(Comment comment, boolean isLiked) {

        String daysAgo = DateUtil.formatElapsedTime(comment.getCreatedAt().toLocalDate());

        return CommentResponseDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .nickname(comment.getUser().getProfile().getNickname())
                .profileImage(comment.getUser().getProfile().getProfileImage())
                .isEdited(comment.isEdited())
                .isLiked(isLiked)
                .likeCount(comment.getActiveLikeCount())
                .daysAgo(daysAgo)
                .build();
    }
}
