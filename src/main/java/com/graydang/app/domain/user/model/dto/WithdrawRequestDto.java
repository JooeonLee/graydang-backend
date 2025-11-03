package com.graydang.app.domain.user.model.dto;

import com.graydang.app.domain.user.model.enums.WithdrawalReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 탈퇴 요청 정보")
public record WithdrawRequestDto(
        @Schema(description = "탈퇴 사유", example = "OTHER")
        @NotNull(message = "탈퇴 사유는 필수입니다.")
        WithdrawalReason reason,
        
        @Schema(description = "기타 사유 상세 내용 (기타 선택 시 필수)", example = "다른 서비스로 이동")
        @Size(max = 500, message = "기타 사유는 500자 이내로 입력해주세요.")
        String otherReason
) {
    public WithdrawRequestDto {
        if (reason == WithdrawalReason.OTHER && (otherReason == null || otherReason.trim().isEmpty())) {
            throw new IllegalArgumentException("기타를 선택한 경우 상세 사유를 입력해야 합니다.");
        }
    }
}