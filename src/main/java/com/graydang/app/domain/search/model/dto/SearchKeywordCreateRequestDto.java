package com.graydang.app.domain.search.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "검색 키워드 생성 요청 DTO")
public class SearchKeywordCreateRequestDto {

    @NotBlank(message = "검색 키워드는 필수입니다.")
    @Size(max = 255, message = "검색 키워드는 255자를 넘을 수 없습니다.")
    @Schema(description = "검색 키워드", example = "국회의원")
    private String text;

    @Schema(description = "표시 여부", example = "true")
    private Boolean display = true;
}