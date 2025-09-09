package com.graydang.app.domain.search.model.dto;

import com.graydang.app.domain.common.Yn;
import com.graydang.app.domain.search.model.SearchKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "검색 키워드 응답 DTO")
public class SearchKeywordResponseDto {

    @Schema(description = "키워드 ID", example = "1")
    private Long id;

    @Schema(description = "검색 키워드", example = "국회의원")
    private String text;

    @Schema(description = "우선순위 (작을수록 우선)", example = "0")
    private int priority;

    @Schema(description = "표시 여부", example = "true")
    private boolean display;

    @Schema(description = "생성 시간", example = "2024-01-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2024-01-01T12:00:00")
    private LocalDateTime updatedAt;

    public static SearchKeywordResponseDto from(SearchKeyword searchKeyword) {
        return SearchKeywordResponseDto.builder()
                .id(searchKeyword.getId())
                .text(searchKeyword.getText())
                .priority(searchKeyword.getPriority())
                .display(searchKeyword.getDisplayYn() == Yn.Y)
                .createdAt(searchKeyword.getCreatedAt())
                .updatedAt(searchKeyword.getUpdatedAt())
                .build();
    }
}