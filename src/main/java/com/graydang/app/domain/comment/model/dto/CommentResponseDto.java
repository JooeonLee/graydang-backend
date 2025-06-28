package com.graydang.app.domain.comment.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "댓글 하나에 대한 응답")
@Builder
public record CommentResponseDto(
        @Schema(
                description = "댓글ID",
                example = "1"
        )
        long commentId,

        @Schema(
                description = "댓글 내용",
                example = "좋은 의안이네요!"
        )
        String content,

        @Schema(
                description = "댓글을 작성한 유저의 닉네임",
                example = "graypick"
        )
        String nickname,

        @Schema(
                description = "댓글을 작성한 유저의 프로필 이미지",
                example = "http://......"
        )
        String profileImage,

        @Schema(
                description = "댓글 수정 여부",
                example = "false"
        )
        boolean isEdited,

        @Schema(
                description = "로그인한 사용자의 해당 댓글에 대한 좋아요 여부",
                example = "false"
        )
        boolean isLiked,

        @Schema(
                description = "해당 댓글에 대한 좋아요 수",
                example = "3"
        )
        long likeCount,

        @Schema(
                description = "조회 시점으로부터 댓글 생성 일자",
                example = "오늘"
        )
        String daysAgo
) {
}
