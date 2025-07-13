package com.graydang.app.domain.bill.model.dto;

import com.graydang.app.global.common.model.dto.SliceResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "의안 검색 결과 응답")
public record BillSearchResponseDto(
        @Schema(
                description = "총 검색 결과수",
                example = "100"
        )
        long totalCount,

        @Schema(
                description = "검색 결과 BillSimpleResponse"
        )
        SliceResponse<BillSimpleResponseDto> bills
) {
}
