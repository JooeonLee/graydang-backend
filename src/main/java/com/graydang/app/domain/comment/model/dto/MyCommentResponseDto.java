package com.graydang.app.domain.comment.model.dto;

import com.graydang.app.domain.comment.repository.projection.CommentSimpleProjection;
import com.graydang.app.domain.comment.repository.projection.MyCommentProjection;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.format.DateTimeFormatter;

public record MyCommentResponseDto(
        @Schema(
                description = "의안ID",
                example = "101"
        )
        Long billId,

        @Schema(
                description = "댓글ID",
                example = "1"
        )
        Long commentId,

        @Schema(
                description = "댓글 내용",
                example = "좋은 의안이네요!"
        )
        String content,

        @Schema(
                description = "댓글 생성 일자",
                example = "2025.03.25"
        )
        String createdDate,

        @Schema(
                description = "해당 댓글에 대한 좋아요 수",
                example = "3"
        )
        Long likeCount,

        @Schema(
                description = "의안 AI 생성 제목",
                example = "해산물 수입을 위한 새로운 법"
        )
        String title
) {

    public static MyCommentResponseDto from(MyCommentProjection projection) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        return new MyCommentResponseDto(
                projection.getBillId(),
                projection.getCommentId(),
                projection.getContent(),
                projection.getCreatedAt().format(formatter),
                projection.getLikeCount(),
                projection.getTitle()
        );
    }
}
