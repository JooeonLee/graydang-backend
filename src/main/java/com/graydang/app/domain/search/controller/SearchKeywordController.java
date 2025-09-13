package com.graydang.app.domain.search.controller;

import com.graydang.app.domain.common.Yn;
import com.graydang.app.domain.search.model.dto.SearchKeywordCreateRequestDto;
import com.graydang.app.domain.search.model.dto.SearchKeywordResponseDto;
import com.graydang.app.domain.search.model.dto.SearchKeywordUpdateRequestDto;
import com.graydang.app.domain.search.service.SearchKeywordService;
import com.graydang.app.global.common.model.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search-keywords")
@Tag(name = "SearchKeyword-Controller", description = "검색 키워드 관리 API")
public class SearchKeywordController {

    private final SearchKeywordService searchKeywordService;

    @Operation(summary = "검색 키워드 생성", description = "새로운 검색 키워드를 생성합니다.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<SearchKeywordResponseDto> createSearchKeyword(
            @Valid @RequestBody SearchKeywordCreateRequestDto requestDto) {
        SearchKeywordResponseDto responseDto = searchKeywordService.createSearchKeyword(requestDto);
        return new BaseResponse<>(responseDto);
    }

    @Operation(summary = "검색 키워드 단건 조회", description = "ID로 검색 키워드를 조회합니다.")
    @GetMapping("/{id}")
    public BaseResponse<SearchKeywordResponseDto> getSearchKeyword(
            @Parameter(description = "검색 키워드 ID", example = "1")
            @PathVariable("id") Long id) {
        SearchKeywordResponseDto responseDto = searchKeywordService.getSearchKeyword(id);
        return new BaseResponse<>(responseDto);
    }

    @Operation(summary = "표시 가능한 검색 키워드 목록 조회", description = "사용자에게 표시할 검색 키워드 목록을 우선순위 순으로 조회합니다.")
    @GetMapping("/display")
    public BaseResponse<List<SearchKeywordResponseDto>> getDisplaySearchKeywords() {
        List<SearchKeywordResponseDto> keywords = searchKeywordService.getDisplaySearchKeywords();
        return new BaseResponse<>(keywords);
    }

    @Operation(summary = "검색 키워드 목록 조회 (관리자용)", description = "검색 조건에 따라 검색 키워드를 페이지네이션하여 조회합니다.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<Page<SearchKeywordResponseDto>> searchKeywords(
            @Parameter(description = "표시 여부 필터 (Y/N)")
            @RequestParam(required = false) Yn displayYn,
            @Parameter(description = "키워드 검색어")
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SearchKeywordResponseDto> keywords = searchKeywordService.searchKeywords(displayYn, keyword, pageable);
        return new BaseResponse<>(keywords);
    }

    @Operation(summary = "검색 키워드 수정", description = "검색 키워드의 정보를 수정합니다.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<SearchKeywordResponseDto> updateSearchKeyword(
            @Parameter(description = "검색 키워드 ID", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody SearchKeywordUpdateRequestDto requestDto) {
        SearchKeywordResponseDto responseDto = searchKeywordService.updateSearchKeyword(id, requestDto);
        return new BaseResponse<>(responseDto);
    }
}