package com.graydang.app.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "소셜 로그인 요청 시 포함해야하는 body값")
public class LoginRequestDto {

    @NotBlank(message = "Authorization Code는 필수입니다.")
    @Schema(description = "Authorization Code", example = "abc123xyz")
    private String code;

    @NotBlank(message = "Redirect URI는 필수입니다.")
    @Schema(description = "Redirect URI", example = "http://localhost:9000/api/auth/oauth")
    private String redirectUri;
}
