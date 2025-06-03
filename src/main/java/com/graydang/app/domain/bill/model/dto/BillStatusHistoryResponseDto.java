package com.graydang.app.domain.bill.model.dto;

import com.graydang.app.domain.bill.model.BillStatusHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "법안 이력 조회 응답")
public class BillStatusHistoryResponseDto {

    @Schema(description = "법안 ID", example = "101")
    private Long id;
    @Schema(description = "단계 순서", example = "1")
    private int stepOrder;
    @Schema(description = "처리 단계 이름", example = "위원회 심사")
    private String stepName;
    @Schema(description = "처리 결과", example = "가결")
    private String stepResult;
    @Schema(description = "처리일", example = "2024-03-25")
    private String stepDate;

    @Builder
    public BillStatusHistoryResponseDto(Long id, int stepOrder, String stepName, String stepResult, String stepDate) {
        this.id = id;
        this.stepOrder = stepOrder;
        this.stepName = stepName;
        this.stepResult = stepResult;
        this.stepDate = stepDate;
    }

    public static BillStatusHistoryResponseDto from(BillStatusHistory billStatusHistory) {
        return buildDto(billStatusHistory.getBill().getId(),
                billStatusHistory.getStepOrder(),
                billStatusHistory.getStepName(),
                billStatusHistory.getStepResult(),
                billStatusHistory.getStepDate().toString());
    }

    public static BillStatusHistoryResponseDto buildZeroOrder(Long id, String stepDate) {
        return buildDto(id,
                0,
                "접수",
                "접수",
                stepDate);
    }

    private static BillStatusHistoryResponseDto buildDto(Long id, int stepOrder, String stepName,
                                                         String stepResult, String stepDate) {
        return BillStatusHistoryResponseDto.builder()
                .id(id)
                .stepOrder(stepOrder)
                .stepName(stepName)
                .stepResult(stepResult)
                .stepDate(stepDate)
                .build();
    }
}
