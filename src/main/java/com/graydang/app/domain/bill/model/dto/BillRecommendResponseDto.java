package com.graydang.app.domain.bill.model.dto;

import com.graydang.app.domain.user.model.InterestKeyword;
import com.graydang.app.global.common.model.dto.SliceResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = "홈 화면 의안 추천 응답")
public record BillRecommendResponseDto(
        @Schema(
                description = "유저 닉네임",
                example = "graypick"
        )
        String nickname,

        @Schema(
                description = "관심 키워드",
                example = "경제"
        )
        Set<String> keywords,

        @Schema(
                description = "추천 법안"
        )
        SliceResponse<BillSimpleResponseDto> bills

) {

    public static BillRecommendResponseDto of(String nickname, Set<String> keywords, SliceResponse<BillSimpleResponseDto> bills) {

        Set<String> keywordLabels = keywords.stream()
                .map(InterestKeyword::fromLabel)
                .map(InterestKeyword::getLabel)
                .collect(Collectors.toSet());

        return new BillRecommendResponseDto(nickname, keywordLabels, bills);
    }
}
