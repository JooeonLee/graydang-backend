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
        long likeReactionCount
) {
}
