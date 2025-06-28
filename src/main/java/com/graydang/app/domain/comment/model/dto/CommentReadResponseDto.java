package com.graydang.app.domain.comment.model.dto;

import com.graydang.app.global.common.model.dto.SliceResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "의안 상세 화면 의안에 대한 댓글 정보 응답")
public record CommentReadResponseDto(
        @Schema(
                description = "의안에 대한 전체 댓글 수",
                example = "7"
        )
        long totalCount,

        @Schema(
                description = "현재 페이지의 댓글 Slice"
        )
        SliceResponse<CommentResponseDto> comments
) {
    public CommentReadResponseDto(long totalCount, SliceResponse<CommentResponseDto> comments) {
        this.totalCount = totalCount;
        this.comments = comments;
    }
}
