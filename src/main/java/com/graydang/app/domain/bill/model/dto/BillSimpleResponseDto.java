package com.graydang.app.domain.bill.model.dto;

import com.graydang.app.domain.bill.model.Bill;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "홈화면, 마이페이지 의안 간단 정보 요청")
public record BillSimpleResponseDto(
        @Schema(
                description = "의안 AI 생성 제목",
                example = "해산물 수입을 위한 새로운 법두"
        )
        String aiTitle,

        @Schema(
                description = "의안 대표 발의자 이름",
                example = "홍길동"
        )
        String representativeName,

        @Schema(
                description = "의안 발의 날짜",
                example = "2025.04.17"
        )
        String proposeDate,

        @Schema(
                description = "의안 진행 상태",
                example = "발의"
        )
        String billHistoryStatus,

        @Schema(
                description = "의안 소관 위원회 이름",
                example = "농림축산식품해양수산위원회"
        )
        String committeeName,

        @Schema(
                description = "의안 조회수",
                example = "233"
        )
        long viewCount,

        @Schema(
                description = "의안 반응 수",
                example = "200"
        )
        long reactionCount,

        @Schema(
                description = "의안 댓글 수",
                example = "16"
        )
        long commentCount,

        @Schema(
                description = "북마크 여부",
                example = "true"
        )
        boolean scraped
) {

    public static BillSimpleResponseDto of(Bill bill, String billHistoryStatus, long reactionCount, long commentCount, boolean scraped) {

        return new BillSimpleResponseDto(
                bill.getAiTitle(),
                bill.getRepresentativeName(),
                bill.getProposeDate().toString(),
                billHistoryStatus,
                bill.getCommitteeName(),
                bill.getViewCount(),
                reactionCount,
                commentCount,
                scraped
        );
    }
}
