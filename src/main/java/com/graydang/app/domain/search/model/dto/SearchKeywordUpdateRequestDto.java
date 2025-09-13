package com.graydang.app.domain.search.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "검색 키워드 수정 요청 DTO")
public class SearchKeywordUpdateRequestDto {

    @Size(max = 255, message = "검색 키워드는 255자를 넘을 수 없습니다.")
    @Schema(description = "검색 키워드", example = "국회의원")
    private String text;

    @Min(value = 0, message = "우선순위는 0 이상이어야 합니다.")
    @Schema(description = "우선순위 (작을수록 우선)", example = "0")
    private Integer priority;

    @Schema(description = "표시 여부", example = "true")
    private Boolean displayYn;

    @Schema(description = "삭제 여부 (true: 소프트 삭제, false: 복구)", example = "false")
    private Boolean delYn;
}