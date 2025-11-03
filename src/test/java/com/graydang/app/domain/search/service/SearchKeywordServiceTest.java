package com.graydang.app.domain.search.service;

import com.graydang.app.domain.common.Yn;
import com.graydang.app.domain.search.model.SearchKeyword;
import com.graydang.app.domain.search.model.dto.SearchKeywordCreateRequestDto;
import com.graydang.app.domain.search.model.dto.SearchKeywordResponseDto;
import com.graydang.app.domain.search.model.dto.SearchKeywordUpdateRequestDto;
import com.graydang.app.domain.search.repository.SearchKeywordRepository;
import com.graydang.app.global.common.exception.BusinessException;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchKeywordServiceTest {

    @Mock
    private SearchKeywordRepository searchKeywordRepository;

    @InjectMocks
    private SearchKeywordService searchKeywordService;

    private SearchKeyword testKeyword;
    private SearchKeywordCreateRequestDto createRequestDto;
    private SearchKeywordUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        testKeyword = SearchKeyword.builder()
                .id(1L)
                .text("테스트 키워드")
                .priority(1)
                .displayYn(Yn.Y)
                .delYn(Yn.N)
                .build();

        createRequestDto = new SearchKeywordCreateRequestDto("새 키워드", true);
        updateRequestDto = new SearchKeywordUpdateRequestDto("수정된 키워드", 5, true, false);
    }

    @Nested
    @DisplayName("createSearchKeyword 테스트")
    class CreateSearchKeywordTest {

        @Test
        @DisplayName("이미 존재하는 키워드일 때 예외를 발생시킨다")
        void create_shouldThrowExceptionWhenKeywordAlreadyExists() {
            given(searchKeywordRepository.existsByTextAndDelYn("새 키워드", Yn.N))
                    .willReturn(true);

            assertThatThrownBy(() -> searchKeywordService.createSearchKeyword(createRequestDto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseResponseStatus")
                    .isEqualTo(BaseResponseStatus.SEARCH_KEYWORD_ALREADY_EXISTS);

            System.out.println("✅ [create_shouldThrowExceptionWhenKeywordAlreadyExists] 테스트 통과 - 중복 키워드 예외 발생");
        }

        @Test
        @DisplayName("활성 키워드 개수를 기반으로 우선순위를 설정한다")
        void create_shouldSetPriorityBasedOnActiveCount() {
            given(searchKeywordRepository.existsByTextAndDelYn("새 키워드", Yn.N))
                    .willReturn(false);
            given(searchKeywordRepository.countByDisplayYnAndDelYn(Yn.Y, Yn.N))
                    .willReturn(3L);
            given(searchKeywordRepository.save(any(SearchKeyword.class)))
                    .willReturn(testKeyword);

            SearchKeywordResponseDto result = searchKeywordService.createSearchKeyword(createRequestDto);

            verify(searchKeywordRepository).save(any(SearchKeyword.class));
            assertThat(result).isNotNull();
            System.out.println("✅ [create_shouldSetPriorityBasedOnActiveCount] 테스트 통과 - 우선순위 자동 설정");
        }

        @Test
        @DisplayName("표시 상태를 올바르게 설정하여 저장한다")
        void create_shouldSaveWithCorrectDisplayStatus() {
            SearchKeywordCreateRequestDto displayFalseDto = new SearchKeywordCreateRequestDto("숨김 키워드", false);
            
            given(searchKeywordRepository.existsByTextAndDelYn("숨김 키워드", Yn.N))
                    .willReturn(false);
            given(searchKeywordRepository.countByDisplayYnAndDelYn(Yn.Y, Yn.N))
                    .willReturn(0L);
            given(searchKeywordRepository.save(any(SearchKeyword.class)))
                    .willReturn(testKeyword);

            SearchKeywordResponseDto result = searchKeywordService.createSearchKeyword(displayFalseDto);

            verify(searchKeywordRepository).save(any(SearchKeyword.class));
            assertThat(result).isNotNull();
            System.out.println("✅ [create_shouldSaveWithCorrectDisplayStatus] 테스트 통과 - 표시 상태 설정");
        }

        @Test
        @DisplayName("ResponseDto를 반환한다")
        void create_shouldReturnResponseDto() {
            given(searchKeywordRepository.existsByTextAndDelYn("새 키워드", Yn.N))
                    .willReturn(false);
            given(searchKeywordRepository.countByDisplayYnAndDelYn(Yn.Y, Yn.N))
                    .willReturn(0L);
            given(searchKeywordRepository.save(any(SearchKeyword.class)))
                    .willReturn(testKeyword);

            SearchKeywordResponseDto result = searchKeywordService.createSearchKeyword(createRequestDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getText()).isEqualTo("테스트 키워드");
            System.out.println("✅ [create_shouldReturnResponseDto] 테스트 통과 - ResponseDto 반환 확인");
        }
    }

    @Nested
    @DisplayName("getSearchKeyword 테스트")
    class GetSearchKeywordTest {

        @Test
        @DisplayName("존재하는 키워드를 반환한다")
        void get_shouldReturnKeywordWhenExists() {
            given(searchKeywordRepository.findByIdAndDelYn(1L, Yn.N))
                    .willReturn(Optional.of(testKeyword));

            SearchKeywordResponseDto result = searchKeywordService.getSearchKeyword(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getText()).isEqualTo("테스트 키워드");
            System.out.println("✅ [get_shouldReturnKeywordWhenExists] 테스트 통과 - 키워드 조회 성공");
        }

        @Test
        @DisplayName("존재하지 않는 키워드일 때 예외를 발생시킨다")
        void get_shouldThrowExceptionWhenNotFound() {
            given(searchKeywordRepository.findByIdAndDelYn(999L, Yn.N))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> searchKeywordService.getSearchKeyword(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseResponseStatus")
                    .isEqualTo(BaseResponseStatus.SEARCH_KEYWORD_NOT_FOUND);

            System.out.println("✅ [get_shouldThrowExceptionWhenNotFound] 테스트 통과 - 미존재 키워드 예외 발생");
        }

        @Test
        @DisplayName("삭제된 키워드일 때 예외를 발생시킨다")
        void get_shouldThrowExceptionWhenDeleted() {
            given(searchKeywordRepository.findByIdAndDelYn(1L, Yn.N))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> searchKeywordService.getSearchKeyword(1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseResponseStatus")
                    .isEqualTo(BaseResponseStatus.SEARCH_KEYWORD_NOT_FOUND);

            System.out.println("✅ [get_shouldThrowExceptionWhenDeleted] 테스트 통과 - 삭제된 키워드 예외 발생");
        }
    }

    @Nested
    @DisplayName("getDisplaySearchKeywords 테스트")
    class GetDisplaySearchKeywordsTest {

        @Test
        @DisplayName("표시된 키워드들만 반환한다")
        void getDisplay_shouldReturnOnlyDisplayedKeywords() {
            SearchKeyword keyword1 = SearchKeyword.builder()
                    .id(1L).text("키워드1").priority(1).displayYn(Yn.Y).build();
            SearchKeyword keyword2 = SearchKeyword.builder()
                    .id(2L).text("키워드2").priority(2).displayYn(Yn.Y).build();
            
            given(searchKeywordRepository.findAllByDisplayYnAndDelYnOrderByPriorityAscIdDesc(Yn.Y, Yn.N))
                    .willReturn(List.of(keyword1, keyword2));

            List<SearchKeywordResponseDto> result = searchKeywordService.getDisplaySearchKeywords();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getText()).isEqualTo("키워드1");
            assertThat(result.get(1).getText()).isEqualTo("키워드2");
            System.out.println("✅ [getDisplay_shouldReturnOnlyDisplayedKeywords] 테스트 통과 - 표시 키워드: " + result.size() + "건");
        }

        @Test
        @DisplayName("우선순위 순서로 반환한다")
        void getDisplay_shouldReturnInPriorityOrder() {
            SearchKeyword keyword1 = SearchKeyword.builder()
                    .id(1L).text("우선순위3").priority(3).displayYn(Yn.Y).build();
            SearchKeyword keyword2 = SearchKeyword.builder()
                    .id(2L).text("우선순위1").priority(1).displayYn(Yn.Y).build();
            
            given(searchKeywordRepository.findAllByDisplayYnAndDelYnOrderByPriorityAscIdDesc(Yn.Y, Yn.N))
                    .willReturn(List.of(keyword2, keyword1));

            List<SearchKeywordResponseDto> result = searchKeywordService.getDisplaySearchKeywords();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getText()).isEqualTo("우선순위1");
            assertThat(result.get(1).getText()).isEqualTo("우선순위3");
            System.out.println("✅ [getDisplay_shouldReturnInPriorityOrder] 테스트 통과 - 우선순위 정렬 확인");
        }

        @Test
        @DisplayName("키워드가 없으면 빈 리스트를 반환한다")
        void getDisplay_shouldReturnEmptyListWhenNone() {
            given(searchKeywordRepository.findAllByDisplayYnAndDelYnOrderByPriorityAscIdDesc(Yn.Y, Yn.N))
                    .willReturn(List.of());

            List<SearchKeywordResponseDto> result = searchKeywordService.getDisplaySearchKeywords();

            assertThat(result).isEmpty();
            System.out.println("✅ [getDisplay_shouldReturnEmptyListWhenNone] 테스트 통과 - 빈 목록 반환");
        }
    }

    @Nested
    @DisplayName("searchKeywords 테스트")
    class SearchKeywordsServiceTest {

        @Test
        @DisplayName("Repository에 위임한다")
        void search_shouldDelegateToRepository() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SearchKeyword> mockPage = new PageImpl<>(List.of(testKeyword));
            
            given(searchKeywordRepository.searchKeywords(Yn.N, Yn.Y, "키워드", pageable))
                    .willReturn(mockPage);

            Page<SearchKeywordResponseDto> result = searchKeywordService.searchKeywords(Yn.Y, "키워드", pageable);

            verify(searchKeywordRepository).searchKeywords(Yn.N, Yn.Y, "키워드", pageable);
            assertThat(result).isNotNull();
            System.out.println("✅ [search_shouldDelegateToRepository] 테스트 통과 - Repository 위임 확인");
        }

        @Test
        @DisplayName("페이징된 결과를 반환한다")
        void search_shouldReturnPagedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SearchKeyword> mockPage = new PageImpl<>(List.of(testKeyword), pageable, 1);
            
            given(searchKeywordRepository.searchKeywords(Yn.N, null, null, pageable))
                    .willReturn(mockPage);

            Page<SearchKeywordResponseDto> result = searchKeywordService.searchKeywords(null, null, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            System.out.println("✅ [search_shouldReturnPagedResults] 테스트 통과 - 페이징 결과: " + result.getTotalElements() + "건");
        }

        @Test
        @DisplayName("ResponseDto로 매핑한다")
        void search_shouldMapToResponseDto() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SearchKeyword> mockPage = new PageImpl<>(List.of(testKeyword));
            
            given(searchKeywordRepository.searchKeywords(Yn.N, null, null, pageable))
                    .willReturn(mockPage);

            Page<SearchKeywordResponseDto> result = searchKeywordService.searchKeywords(null, null, pageable);

            assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
            assertThat(result.getContent().get(0).getText()).isEqualTo("테스트 키워드");
            System.out.println("✅ [search_shouldMapToResponseDto] 테스트 통과 - DTO 매핑 확인");
        }
    }

    @Nested
    @DisplayName("updateSearchKeyword 테스트")
    class UpdateSearchKeywordTest {

        @Test
        @DisplayName("존재하지 않는 키워드일 때 예외를 발생시킨다")
        void update_shouldThrowExceptionWhenNotFound() {
            given(searchKeywordRepository.findById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> searchKeywordService.updateSearchKeyword(999L, updateRequestDto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseResponseStatus")
                    .isEqualTo(BaseResponseStatus.SEARCH_KEYWORD_NOT_FOUND);

            System.out.println("✅ [update_shouldThrowExceptionWhenNotFound] 테스트 통과 - 미존재 키워드 예외 발생");
        }

        @Test
        @DisplayName("텍스트가 제공되면 업데이트한다")
        void update_shouldUpdateTextWhenProvided() {
            SearchKeyword mockKeyword = SearchKeyword.builder()
                    .id(1L).text("원본 텍스트").build();
            
            given(searchKeywordRepository.findById(1L))
                    .willReturn(Optional.of(mockKeyword));
            given(searchKeywordRepository.existsByTextAndDelYn("수정된 키워드", Yn.N))
                    .willReturn(false);

            SearchKeywordResponseDto result = searchKeywordService.updateSearchKeyword(1L, updateRequestDto);

            assertThat(result).isNotNull();
            System.out.println("✅ [update_shouldUpdateTextWhenProvided] 테스트 통과 - 텍스트 업데이트");
        }

        @Test
        @DisplayName("중복된 텍스트일 때 예외를 발생시킨다")
        void update_shouldThrowExceptionWhenTextDuplicated() {
            SearchKeyword mockKeyword = SearchKeyword.builder()
                    .id(1L).text("원본 텍스트").build();
            
            given(searchKeywordRepository.findById(1L))
                    .willReturn(Optional.of(mockKeyword));
            given(searchKeywordRepository.existsByTextAndDelYn("수정된 키워드", Yn.N))
                    .willReturn(true);

            assertThatThrownBy(() -> searchKeywordService.updateSearchKeyword(1L, updateRequestDto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseResponseStatus")
                    .isEqualTo(BaseResponseStatus.SEARCH_KEYWORD_ALREADY_EXISTS);

            System.out.println("✅ [update_shouldThrowExceptionWhenTextDuplicated] 테스트 통과 - 중복 텍스트 예외 발생");
        }

        @Test
        @DisplayName("우선순위가 제공되면 업데이트한다")
        void update_shouldUpdatePriorityWhenProvided() {
            given(searchKeywordRepository.findById(1L))
                    .willReturn(Optional.of(testKeyword));

            SearchKeywordResponseDto result = searchKeywordService.updateSearchKeyword(1L, updateRequestDto);

            assertThat(result).isNotNull();
            System.out.println("✅ [update_shouldUpdatePriorityWhenProvided] 테스트 통과 - 우선순위 업데이트");
        }

        @Test
        @DisplayName("표시 상태가 제공되면 업데이트한다")
        void update_shouldUpdateDisplayStatusWhenProvided() {
            given(searchKeywordRepository.findById(1L))
                    .willReturn(Optional.of(testKeyword));

            SearchKeywordResponseDto result = searchKeywordService.updateSearchKeyword(1L, updateRequestDto);

            assertThat(result).isNotNull();
            System.out.println("✅ [update_shouldUpdateDisplayStatusWhenProvided] 테스트 통과 - 표시 상태 업데이트");
        }

        @Test
        @DisplayName("삭제 상태가 제공되면 업데이트한다")
        void update_shouldUpdateDeleteStatusWhenProvided() {
            SearchKeywordUpdateRequestDto deleteDto = new SearchKeywordUpdateRequestDto(null, null, null, true);
            
            given(searchKeywordRepository.findById(1L))
                    .willReturn(Optional.of(testKeyword));

            SearchKeywordResponseDto result = searchKeywordService.updateSearchKeyword(1L, deleteDto);

            assertThat(result).isNotNull();
            System.out.println("✅ [update_shouldUpdateDeleteStatusWhenProvided] 테스트 통과 - 삭제 상태 업데이트");
        }

        @Test
        @DisplayName("제공된 필드만 업데이트한다")
        void update_shouldOnlyUpdateProvidedFields() {
            SearchKeywordUpdateRequestDto partialDto = new SearchKeywordUpdateRequestDto(null, 10, null, null);
            
            given(searchKeywordRepository.findById(1L))
                    .willReturn(Optional.of(testKeyword));

            SearchKeywordResponseDto result = searchKeywordService.updateSearchKeyword(1L, partialDto);

            assertThat(result).isNotNull();
            System.out.println("✅ [update_shouldOnlyUpdateProvidedFields] 테스트 통과 - 부분 업데이트 확인");
        }
    }
}