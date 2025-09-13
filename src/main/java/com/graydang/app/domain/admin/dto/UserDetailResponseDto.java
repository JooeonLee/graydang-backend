package com.graydang.app.domain.admin.dto;

import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.model.UserCredential;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "어드민 유저 상세 정보 응답 DTO")
public class UserDetailResponseDto {

    @Schema(description = "유저 ID")
    private Long id;

    @Schema(description = "유저명")
    private String username;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "역할")
    private String role;

    @Schema(description = "상태")
    private String status;

    @Schema(description = "프로필 정보")
    private ProfileInfo profile;

    @Schema(description = "인증 제공자 목록")
    private List<CredentialInfo> credentials;

    @Schema(description = "가입일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "프로필 정보")
    public static class ProfileInfo {
        @Schema(description = "프로필 이미지")
        private String profileImage;

        @Schema(description = "닉네임")
        private String nickname;

        @Schema(description = "관심 키워드 1")
        private String keyword1;

        @Schema(description = "관심 키워드 2")
        private String keyword2;

        @Schema(description = "관심 키워드 3")
        private String keyword3;

        @Schema(description = "관심 키워드 4")
        private String keyword4;

        @Schema(description = "관심 키워드 5")
        private String keyword5;

        @Schema(description = "프로필 상태")
        private String status;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "인증 정보")
    public static class CredentialInfo {
        @Schema(description = "제공자")
        private String provider;

        @Schema(description = "제공자 유저 ID")
        private String providerUserId;

        @Schema(description = "상태")
        private String status;
    }

    public static UserDetailResponseDto from(User user) {
        ProfileInfo profileInfo = null;
        if (user.getProfile() != null) {
            profileInfo = ProfileInfo.builder()
                    .profileImage(user.getProfile().getProfileImage())
                    .nickname(user.getProfile().getNickname())
                    .keyword1(user.getProfile().getKeyword1())
                    .keyword2(user.getProfile().getKeyword2())
                    .keyword3(user.getProfile().getKeyword3())
                    .keyword4(user.getProfile().getKeyword4())
                    .keyword5(user.getProfile().getKeyword5())
                    .status(user.getProfile().getStatus())
                    .build();
        }

        List<CredentialInfo> credentialInfos = user.getCredentials().stream()
                .map(credential -> CredentialInfo.builder()
                        .provider(credential.getProvider())
                        .providerUserId(credential.getProviderUserId())
                        .status(credential.getStatus())
                        .build())
                .collect(Collectors.toList());

        return UserDetailResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .profile(profileInfo)
                .credentials(credentialInfos)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}