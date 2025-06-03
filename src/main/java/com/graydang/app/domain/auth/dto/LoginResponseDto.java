package com.graydang.app.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@AllArgsConstructor
@Schema(description = "소셜 로그인 성공 시 반환되는 응답")
public class LoginResponseDto {

    @Schema(description = "Access Token (JWT)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Refresh Token (JWT)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "로그인한 사용자의 User ID", example = "1")
    private Long userid;

    @Schema(description = "로그인한 사용자의 온보딩 진행 여부", example = "false")
    private boolean onboarded;
}
