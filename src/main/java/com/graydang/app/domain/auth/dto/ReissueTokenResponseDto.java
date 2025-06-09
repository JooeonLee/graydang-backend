package com.graydang.app.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 응답 DTO")
public record ReissueTokenResponseDto(
        @Schema(description = "Access 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
        String accessToken,
        @Schema(description = "Refresh 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
        String refreshToken
) {
}
