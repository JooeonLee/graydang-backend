package com.graydang.app.domain.admin.dto;

import com.graydang.app.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "어드민 유저 목록 응답 DTO")
public class UserListResponseDto {

    @Schema(description = "유저 ID")
    private Long id;

    @Schema(description = "유저명")
    private String username;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "역할")
    private String role;

    @Schema(description = "상태")
    private String status;

    @Schema(description = "가입일시")
    private LocalDateTime createdAt;

    public static UserListResponseDto from(User user) {
        return UserListResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getProfile() != null ? user.getProfile().getNickname() : null)
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}