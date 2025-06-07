package com.graydang.app.global.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Map;

@Getter
@Schema(description = "유효성 검사 실패 응답")
public class ValidationErrorResponse {

    @Schema(description = "요청 성공 여부", example = "false")
    private final boolean isSuccess = false;

    @Schema(description = "응답 코드", example = "2001")
    private final int responseCode = 2001;

    @Schema(description = "응답 메시지", example = "요청 파라미터 타입이 올바르지 않습니다.")
    private final String responseMessage = "요청 파라미터 타입이 올바르지 않습니다.";

    @Schema(description = "필드별 에러 메시지", example = "{\"nickname\": \"크기가 3에서 8 사이여야 합니다.\"}")
    private final Map<String, String> result;

    public ValidationErrorResponse(Map<String, String> result) {
        this.result = result;
    }
}
