package com.graydang.app.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 요청 DTO")
public record ReissueTokenRequestDto(
        @Schema(description = "기존 Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR...")
        String refreshToken
) {
}
