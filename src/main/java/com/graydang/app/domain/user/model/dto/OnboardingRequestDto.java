package com.graydang.app.domain.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "온보딩 요청")
public record OnboardingRequestDto(
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(message = "닉네임은 3자 이상 8자 이하로 입력해주세요.")
        String nickname,

        @Size(min = 5, max = 5, message = "관심 키워드는 정확히 5개 선택해야 합니다.")
        List<@NotBlank String> interestKeywords
) {
}
