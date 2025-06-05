package com.graydang.app.domain.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "닉네임 중복 확인 요청")
public record NicknameCheckRequestDto(
        @NotBlank
        @Size(min = 3, max = 8)
        @Schema(description = "사용자 닉네임", example = "graypick")
        String nickname
) {
}
