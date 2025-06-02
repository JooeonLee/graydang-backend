package com.graydang.app.domain.bill.model.dto;

import com.graydang.app.domain.bill.model.Bill;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "법안 상세 조회 응답")
public class BillDetailResponseDto {

    @Schema(description = "법안 ID", example = "1")
    private Long id;
    @Schema(description = "AI 생성 법안 제목", example = "공직자 이해충돌 방지법")
    private String billAiTitle;
    @Schema(description = "대표 발의자 이름", example = "홍길동")
    private String representativeName;
    @Schema(description = "법안 상태", example = "접수")
    private String billStatus;
    @Schema(description = "소관 위원회", example = "정무위원회")
    private String committeeName;
    @Schema(description = "AI 생성 법안 요약", example = "공직자의 이해충돌 방지를 위한 법률 제정안")
    private String billAiSummary;
    @Schema(description = "법안 요약")
    private String billSummary;
    @Schema(description = "제안일", example = "2024-03-10")
    private String proposeDate;
    @Schema(description = "조회수", example = "1")
    private long viewCount;

    @Schema(description = "반응 수", example = "10")
    private long reactionCount;
    @Schema(description = "댓글 수", example = "5")
    private long commentCount;
    @Schema(description = "북마크 여부", example = "true")
    private boolean isScrapped;

    @Schema(description = "처리 이력")
    private List<BillStatusHistoryResponseDto> history;

    @Builder
    public BillDetailResponseDto(Long id, String billAiTitle, String representativeName, String billStatus,
                                 String committeeName, String billAiSummary, String billSummary, String proposeDate,
                                 long viewCount, long reactionCount, long commentCount, boolean isScrapped,
                                 List<BillStatusHistoryResponseDto> history) {
        this.id = id;
        this.billAiTitle = billAiTitle;
        this.representativeName = representativeName;
        this.billStatus = billStatus;
        this.committeeName = committeeName;
        this.billAiSummary = billAiSummary;
        this.billSummary = billSummary;
        this.proposeDate = proposeDate;
        this.viewCount = viewCount;

        this.reactionCount = reactionCount;
        this.commentCount = commentCount;
        this.isScrapped = isScrapped;

        this.history = history;
    }

    public static BillDetailResponseDto of(Bill bill, long reactionCount, long commentCount, boolean isScrapped,
                                           List<BillStatusHistoryResponseDto> history) {
        return BillDetailResponseDto.builder()
                .id(bill.getId())
                .billAiTitle(bill.getAiTitle())
                .billAiSummary(bill.getAiSummary())
                .billSummary(bill.getSummary())
                .representativeName(bill.getRepresentativeName())
                .billStatus(bill.getBillStatus())
                .committeeName(bill.getCommitteeName())
                .proposeDate(bill.getProposeDate().toString())
                .viewCount(bill.getViewCount())

                .reactionCount(reactionCount)
                .commentCount(commentCount)
                .isScrapped(isScrapped)

                .history(history)
                .build();
    }
}
