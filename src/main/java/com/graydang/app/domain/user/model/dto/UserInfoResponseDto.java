package com.graydang.app.domain.user.model.dto;

import com.graydang.app.domain.user.model.InterestKeyword;
import com.graydang.app.domain.user.model.UserProfile;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Schema(description = "마이페이지 기본 정보 요청")
public record UserInfoResponseDto(
        @Schema(
                description = "사용자 닉네임",
                example = "graypick"
        )
        String nickname,

        @Schema(
                description = "사용자 프로필 이미지",
                example = "https://example.profile.image"
        )
        String profileImageUrl,

        @ArraySchema(
                schema = @Schema(description = "관심 키워드", example = "경제")
        )
        List<String> interests,

        @Schema(
                description = "사용자가 북마크한 의안 수",
                example = "5"
        )
        long scrapeCount,

        @Schema(
                description = "사용자가 표시한 의견 수(의안에 대한 반응 수)",
                example = "10"
        )
        long reactionCount,

        @Schema(
                description = "사용자가 만든 댓글 수",
                example = "15"
        )
        long commentCount
) {

    public static UserInfoResponseDto of(UserProfile profile, long scrapeCount, long reactionCount, long commentCount) {
        List<String> interests = Stream.of(
                        profile.getKeyword1(),
                        profile.getKeyword2(),
                        profile.getKeyword3(),
                        profile.getKeyword4(),
                        profile.getKeyword5()
                )
                .filter(Objects::nonNull)
                .map(InterestKeyword::fromName)
                .map(InterestKeyword::getLabel)
                .toList();

        return new UserInfoResponseDto(
                profile.getNickname(),
                profile.getProfileImage(),
                interests,
                scrapeCount,
                reactionCount,
                commentCount
        );
    }
}
