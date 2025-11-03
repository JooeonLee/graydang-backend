package com.graydang.app.domain.search.service;

import com.graydang.app.domain.common.Yn;
import com.graydang.app.domain.search.model.SearchKeyword;
import com.graydang.app.domain.search.model.dto.SearchKeywordCreateRequestDto;
import com.graydang.app.domain.search.model.dto.SearchKeywordResponseDto;
import com.graydang.app.domain.search.model.dto.SearchKeywordUpdateRequestDto;
import com.graydang.app.domain.search.repository.SearchKeywordRepository;
import com.graydang.app.global.common.exception.BusinessException;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchKeywordService {

    private final SearchKeywordRepository searchKeywordRepository;

    @Transactional
    public SearchKeywordResponseDto createSearchKeyword(SearchKeywordCreateRequestDto requestDto) {
        if (searchKeywordRepository.existsByTextAndDelYn(requestDto.getText(), Yn.N)) {
            throw new BusinessException(BaseResponseStatus.SEARCH_KEYWORD_ALREADY_EXISTS);
        }

        // 표시 여부에 따라 우선순위 설정
        int priority;
        if (requestDto.getDisplay()) {
            // 표시 키워드: 기존 표시 키워드 개수 + 1
            long activeDisplayedCount = searchKeywordRepository.countByDisplayYnAndDelYn(Yn.Y, Yn.N);
            priority = (int) (activeDisplayedCount + 1);
        } else {
            // 비표시 키워드: 기존 비표시 키워드 개수 + 1
            long activeHiddenCount = searchKeywordRepository.countByDisplayYnAndDelYn(Yn.N, Yn.N);
            priority = (int) (activeHiddenCount + 1);
        }

        SearchKeyword searchKeyword = SearchKeyword.builder()
                .text(requestDto.getText())
                .priority(priority)
                .displayYn(requestDto.getDisplay() ? Yn.Y : Yn.N)
                .build();

        SearchKeyword savedKeyword = searchKeywordRepository.save(searchKeyword);
        return SearchKeywordResponseDto.from(savedKeyword);
    }

    public SearchKeywordResponseDto getSearchKeyword(Long id) {
        SearchKeyword searchKeyword = searchKeywordRepository.findByIdAndDelYn(id, Yn.N)
                .orElseThrow(() -> new BusinessException(BaseResponseStatus.SEARCH_KEYWORD_NOT_FOUND));

        return SearchKeywordResponseDto.from(searchKeyword);
    }

    public List<SearchKeywordResponseDto> getDisplaySearchKeywords() {
        List<SearchKeyword> keywords = searchKeywordRepository
                .findAllByDisplayYnAndDelYnOrderByPriorityAscIdDesc(Yn.Y, Yn.N);

        return keywords.stream()
                .map(SearchKeywordResponseDto::from)
                .collect(Collectors.toList());
    }

    public Page<SearchKeywordResponseDto> searchKeywords(Yn displayYn, String keyword, Pageable pageable) {
        Page<SearchKeyword> keywordPage = searchKeywordRepository
                .searchKeywords(Yn.N, displayYn, keyword, pageable);

        return keywordPage.map(SearchKeywordResponseDto::from);
    }

    @Transactional
    public SearchKeywordResponseDto updateSearchKeyword(Long id, SearchKeywordUpdateRequestDto requestDto) {
        SearchKeyword searchKeyword = searchKeywordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BaseResponseStatus.SEARCH_KEYWORD_NOT_FOUND));

        if (requestDto.getText() != null) {
            if (!searchKeyword.getText().equals(requestDto.getText()) &&
                searchKeywordRepository.existsByTextAndDelYn(requestDto.getText(), Yn.N)) {
                throw new BusinessException(BaseResponseStatus.SEARCH_KEYWORD_ALREADY_EXISTS);
            }
            searchKeyword.changeText(requestDto.getText());
        }

        if (requestDto.getPriority() != null) {
            searchKeyword.changePriority(requestDto.getPriority());
        }

        if (requestDto.getDisplayYn() != null) {
            if (requestDto.getDisplayYn()) {
                searchKeyword.show();
            } else {
                searchKeyword.hide();
            }
        }

        if (requestDto.getDelYn() != null) {
            if (requestDto.getDelYn()) {
                searchKeyword.softDelete();
            } else {
                searchKeyword.restore();
            }
        }

        return SearchKeywordResponseDto.from(searchKeyword);
    }
}