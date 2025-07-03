package com.graydang.app.domain.bill.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BillReactionCountResponseDto(
        @Schema(
                description = "의안에 대한 흥미진진 반응 수",
                example = "5"
        )
        long excitedReactionCount,

        @Schema(
                description = "의안에 대한 개선필요 반응 수",
                example = "5"
        )
        long improvementReactionCount,

        @Schema(
                description = "의안에 대한 아쉬워요 반응 수",
                example = "5"
        )
        long disappointedReactionCount,

        @Schema(
                description = "의안에 대한 좋아요 반응 수",
                example = "5"
        )
        long likeReactionCount,

        @Schema(
                description = "로그인한 유저의 반응 (반응하지 않았을 경우 null, 비로그인 유저의 경우 null)",
                example = "좋아요"
        )
        String userReactionType
) {
}
