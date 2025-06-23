package com.graydang.app.domain.bill.model.dto;

import com.graydang.app.domain.bill.model.BillReaction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.format.DateTimeFormatter;

@Schema(description = "마이페이지 의안 반응 정보 요청")
public record BillReactionSimpleResponseDto (
        @Schema(
                description = "의안 AI 생성 제목",
                example = "해산물 수입을 위한 새로운 법두"
        )
        String aiTitle,

        @Schema(
                description = "반응한 의안 ID",
                example = "101"
        )
        long billId,

        @Schema(
                description = "의안 반응 타입",
                example = "좋아요"
        )
        String reactionType,

        @Schema(
                description = "의안 반응 생성 날짜",
                example = "2025.05.20"
        )
        String date
) {

    public static BillReactionSimpleResponseDto from(BillReaction billReaction, String aiTitle) {

        return new BillReactionSimpleResponseDto(
                aiTitle,
                billReaction.getBill().getId(),
                billReaction.getReactionType().getDisplayName(),
                billReaction.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        );
    }
}
