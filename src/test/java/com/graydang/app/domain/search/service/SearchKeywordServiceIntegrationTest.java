package com.graydang.app.domain.search.service;

import com.graydang.app.domain.common.Yn;
import com.graydang.app.domain.search.model.SearchKeyword;
import com.graydang.app.domain.search.model.dto.SearchKeywordCreateRequestDto;
import com.graydang.app.domain.search.model.dto.SearchKeywordResponseDto;
import com.graydang.app.domain.search.model.dto.SearchKeywordUpdateRequestDto;
import com.graydang.app.domain.search.repository.SearchKeywordRepository;
import com.graydang.app.global.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(SearchKeywordService.class)
class SearchKeywordServiceIntegrationTest {

    @Autowired
    private SearchKeywordService searchKeywordService;

    @Autowired
    private SearchKeywordRepository searchKeywordRepository;

    @BeforeEach
    void setUp() {
        searchKeywordRepository.deleteAll();
    }

    @Nested
    @DisplayName("트랜잭션 통합 테스트")
    class TransactionIntegrationTest {

        @Test
        @DisplayName("예외 발생 시 롤백된다")
        void create_shouldRollbackOnException() {
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("기존 키워드")
                    .delYn(Yn.N)
                    .build());

            SearchKeywordCreateRequestDto duplicateDto = new SearchKeywordCreateRequestDto("기존 키워드", true);

            assertThatThrownBy(() -> searchKeywordService.createSearchKeyword(duplicateDto))
                    .isInstanceOf(BusinessException.class);

            long count = searchKeywordRepository.count();
            assertThat(count).isEqualTo(1);
            System.out.println("✅ [create_shouldRollbackOnException] 테스트 통과 - 롤백 확인, 총 키워드: " + count + "건");
        }

        @Test
        @DisplayName("수정 사항이 영속화된다")
        void update_shouldPersistChanges() {
            SearchKeyword savedKeyword = searchKeywordRepository.save(SearchKeyword.builder()
                    .text("원본 키워드")
                    .priority(1)
                    .displayYn(Yn.Y)
                    .delYn(Yn.N)
                    .build());

            SearchKeywordUpdateRequestDto updateDto = new SearchKeywordUpdateRequestDto(
                    "수정된 키워드", 10, false, null
            );

            searchKeywordService.updateSearchKeyword(savedKeyword.getId(), updateDto);

            Optional<SearchKeyword> updatedKeyword = searchKeywordRepository.findById(savedKeyword.getId());
            assertThat(updatedKeyword).isPresent();
            assertThat(updatedKeyword.get().getText()).isEqualTo("수정된 키워드");
            assertThat(updatedKeyword.get().getPriority()).isEqualTo(10);
            assertThat(updatedKeyword.get().getDisplayYn()).isEqualTo(Yn.N);
            System.out.println("✅ [update_shouldPersistChanges] 테스트 통과 - 변경사항 영속화 확인");
        }

        @Test
        @DisplayName("소프트 삭제는 실제로 레코드를 삭제하지 않는다")
        void softDelete_shouldNotActuallyDeleteRecord() {
            SearchKeyword savedKeyword = searchKeywordRepository.save(SearchKeyword.builder()
                    .text("삭제할 키워드")
                    .delYn(Yn.N)
                    .build());

            SearchKeywordUpdateRequestDto deleteDto = new SearchKeywordUpdateRequestDto(
                    null, null, null, true
            );

            searchKeywordService.updateSearchKeyword(savedKeyword.getId(), deleteDto);

            Optional<SearchKeyword> keyword = searchKeywordRepository.findById(savedKeyword.getId());
            assertThat(keyword).isPresent();
            assertThat(keyword.get().getDelYn()).isEqualTo(Yn.Y);

            long totalCount = searchKeywordRepository.count();
            assertThat(totalCount).isEqualTo(1);
            System.out.println("✅ [softDelete_shouldNotActuallyDeleteRecord] 테스트 통과 - 소프트 삭제 확인, 총 레코드: " + totalCount + "건");
        }
    }

    @Nested
    @DisplayName("우선순위 시나리오 테스트")
    class PriorityScenarioTest {

        @Test
        @DisplayName("여러 키워드 생성 시 우선순위가 증분된다")
        void multipleCreate_shouldAssignIncrementalPriorities() {
            SearchKeywordCreateRequestDto dto1 = new SearchKeywordCreateRequestDto("키워드1", true);
            SearchKeywordCreateRequestDto dto2 = new SearchKeywordCreateRequestDto("키워드2", true);
            SearchKeywordCreateRequestDto dto3 = new SearchKeywordCreateRequestDto("키워드3", false);

            SearchKeywordResponseDto result1 = searchKeywordService.createSearchKeyword(dto1);
            SearchKeywordResponseDto result2 = searchKeywordService.createSearchKeyword(dto2);
            SearchKeywordResponseDto result3 = searchKeywordService.createSearchKeyword(dto3);

            assertThat(result1.getPriority()).isEqualTo(1);
            assertThat(result2.getPriority()).isEqualTo(2);
            assertThat(result3.getPriority()).isEqualTo(1);
            System.out.println("✅ [multipleCreate_shouldAssignIncrementalPriorities] 테스트 통과 - 우선순위: " + 
                    result1.getPriority() + ", " + result2.getPriority() + ", " + result3.getPriority());
        }

        @Test
        @DisplayName("우선순위 변경은 정렬에 영향을 준다")
        void priorityChange_shouldNotAffectSorting() {
            SearchKeyword keyword1 = searchKeywordRepository.save(SearchKeyword.builder()
                    .text("키워드1").priority(1).displayYn(Yn.Y).build());
            SearchKeyword keyword2 = searchKeywordRepository.save(SearchKeyword.builder()
                    .text("키워드2").priority(2).displayYn(Yn.Y).build());

            SearchKeywordUpdateRequestDto updateDto = new SearchKeywordUpdateRequestDto(
                    null, 0, null, null
            );
            searchKeywordService.updateSearchKeyword(keyword2.getId(), updateDto);

            List<SearchKeywordResponseDto> displayKeywords = searchKeywordService.getDisplaySearchKeywords();
            assertThat(displayKeywords).hasSize(2);
            assertThat(displayKeywords.get(0).getText()).isEqualTo("키워드2");
            assertThat(displayKeywords.get(1).getText()).isEqualTo("키워드1");
            System.out.println("✅ [priorityChange_shouldNotAffectSorting] 테스트 통과 - 우선순위 변경 후 정렬: " + displayKeywords.size() + "건");
        }

        @Test
        @DisplayName("삭제 후 생성 시 우선순위가 재계산된다")
        void deleteAndCreate_shouldRecalculatePriority() {
            SearchKeyword keyword1 = searchKeywordRepository.save(SearchKeyword.builder()
                    .text("키워드1").displayYn(Yn.Y).build());
            SearchKeyword keyword2 = searchKeywordRepository.save(SearchKeyword.builder()
                    .text("키워드2").displayYn(Yn.Y).build());

            SearchKeywordUpdateRequestDto deleteDto = new SearchKeywordUpdateRequestDto(
                    null, null, null, true
            );
            searchKeywordService.updateSearchKeyword(keyword1.getId(), deleteDto);

            SearchKeywordCreateRequestDto newDto = new SearchKeywordCreateRequestDto("새 키워드", true);
            SearchKeywordResponseDto newKeyword = searchKeywordService.createSearchKeyword(newDto);

            assertThat(newKeyword.getPriority()).isEqualTo(2);
            System.out.println("✅ [deleteAndCreate_shouldRecalculatePriority] 테스트 통과 - 삭제 후 생성된 키워드 우선순위: " + newKeyword.getPriority());
        }
    }

    @Nested
    @DisplayName("복합 시나리오 테스트")
    class ComplexScenarioTest {

        @Test
        @DisplayName("키워드 생성, 수정, 삭제, 복구 전체 플로우")
        void fullLifecycleTest() {
            SearchKeywordCreateRequestDto createDto = new SearchKeywordCreateRequestDto("라이프사이클 테스트", true);
            SearchKeywordResponseDto created = searchKeywordService.createSearchKeyword(createDto);

            SearchKeywordUpdateRequestDto updateDto = new SearchKeywordUpdateRequestDto(
                    "수정된 라이프사이클", 5, false, null
            );
            SearchKeywordResponseDto updated = searchKeywordService.updateSearchKeyword(created.getId(), updateDto);

            SearchKeywordUpdateRequestDto deleteDto = new SearchKeywordUpdateRequestDto(
                    null, null, null, true
            );
            searchKeywordService.updateSearchKeyword(created.getId(), deleteDto);

            SearchKeywordUpdateRequestDto restoreDto = new SearchKeywordUpdateRequestDto(
                    null, null, null, false
            );
            SearchKeywordResponseDto restored = searchKeywordService.updateSearchKeyword(created.getId(), restoreDto);

            assertThat(restored.getText()).isEqualTo("수정된 라이프사이클");
            assertThat(restored.getPriority()).isEqualTo(5);
            assertThat(restored.isDisplay()).isFalse();
            System.out.println("✅ [fullLifecycleTest] 테스트 통과 - 전체 라이프사이클 검증 완료");
        }

        @Test
        @DisplayName("다중 필터 검색 시나리오")
        void multipleFilterSearchScenario() {
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("법안 검색").displayYn(Yn.Y).delYn(Yn.N).build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("법안 분석").displayYn(Yn.N).delYn(Yn.N).build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("국회 법안").displayYn(Yn.Y).delYn(Yn.Y).build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("예산안").displayYn(Yn.Y).delYn(Yn.N).build());

            List<SearchKeywordResponseDto> displayedKeywords = searchKeywordService.getDisplaySearchKeywords();

            assertThat(displayedKeywords).hasSize(2);
            assertThat(displayedKeywords).extracting("text")
                    .containsExactlyInAnyOrder("법안 검색", "예산안");
            System.out.println("✅ [multipleFilterSearchScenario] 테스트 통과 - 다중 필터 결과: " + displayedKeywords.size() + "건");
        }
    }
}