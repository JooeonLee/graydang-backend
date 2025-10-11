package com.graydang.app.domain.search.repository;

import com.graydang.app.domain.common.Yn;
import com.graydang.app.domain.search.model.SearchKeyword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SearchKeywordRepositoryTest {

    @Autowired
    private SearchKeywordRepository searchKeywordRepository;

    @BeforeEach
    void setUp() {
        searchKeywordRepository.deleteAll();
    }

    @Nested
    @DisplayName("findByIdAndDelYn 테스트")
    class FindByIdAndDelYnTest {

        @Test
        @DisplayName("존재하는 키워드를 찾는다")
        void findByIdAndDelYn_shouldReturnKeywordWhenExists() {
            SearchKeyword savedKeyword = searchKeywordRepository.save(
                    SearchKeyword.builder()
                            .text("테스트 키워드")
                            .priority(1)
                            .displayYn(Yn.Y)
                            .delYn(Yn.N)
                            .build()
            );

            Optional<SearchKeyword> found = searchKeywordRepository.findByIdAndDelYn(
                    savedKeyword.getId(), Yn.N
            );

            assertThat(found).isPresent();
            assertThat(found.get().getText()).isEqualTo("테스트 키워드");
            System.out.println("✅ [findByIdAndDelYn_shouldReturnKeywordWhenExists] 테스트 통과 - 키워드 조회 성공");
        }

        @Test
        @DisplayName("삭제된 키워드는 반환하지 않는다")
        void findByIdAndDelYn_shouldReturnEmptyWhenDeleted() {
            SearchKeyword savedKeyword = searchKeywordRepository.save(
                    SearchKeyword.builder()
                            .text("삭제된 키워드")
                            .delYn(Yn.Y)
                            .build()
            );

            Optional<SearchKeyword> found = searchKeywordRepository.findByIdAndDelYn(
                    savedKeyword.getId(), Yn.N
            );

            assertThat(found).isEmpty();
            System.out.println("✅ [findByIdAndDelYn_shouldReturnEmptyWhenDeleted] 테스트 통과 - 삭제된 키워드 필터링 확인");
        }

        @Test
        @DisplayName("존재하지 않는 ID는 빈 결과를 반환한다")
        void findByIdAndDelYn_shouldReturnEmptyWhenNotFound() {
            Optional<SearchKeyword> found = searchKeywordRepository.findByIdAndDelYn(999L, Yn.N);

            assertThat(found).isEmpty();
            System.out.println("✅ [findByIdAndDelYn_shouldReturnEmptyWhenNotFound] 테스트 통과 - 미존재 ID 처리 확인");
        }
    }

    @Nested
    @DisplayName("findAllByDisplayYnAndDelYn 정렬 테스트")
    class FindAllByDisplayYnAndDelYnTest {

        @Test
        @DisplayName("우선순위 오름차순, ID 내림차순으로 정렬한다")
        void findAll_shouldReturnInPriorityOrder() {
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("키워드1").priority(3).displayYn(Yn.Y).build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("키워드2").priority(1).displayYn(Yn.Y).build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("키워드3").priority(2).displayYn(Yn.Y).build());

            List<SearchKeyword> keywords = searchKeywordRepository
                    .findAllByDisplayYnAndDelYnOrderByPriorityAscIdDesc(Yn.Y, Yn.N);

            assertThat(keywords).hasSize(3);
            assertThat(keywords.get(0).getText()).isEqualTo("키워드2");
            assertThat(keywords.get(1).getText()).isEqualTo("키워드3");
            assertThat(keywords.get(2).getText()).isEqualTo("키워드1");
            System.out.println("✅ [findAll_shouldReturnInPriorityOrder] 테스트 통과 - 정렬 순서: " + keywords.size() + "건");
        }

        @Test
        @DisplayName("표시된 키워드만 반환한다")
        void findAll_shouldReturnOnlyDisplayedKeywords() {
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("표시됨").displayYn(Yn.Y).build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("숨김").displayYn(Yn.N).build());

            List<SearchKeyword> keywords = searchKeywordRepository
                    .findAllByDisplayYnAndDelYnOrderByPriorityAscIdDesc(Yn.Y, Yn.N);

            assertThat(keywords).hasSize(1);
            assertThat(keywords.get(0).getText()).isEqualTo("표시됨");
            System.out.println("✅ [findAll_shouldReturnOnlyDisplayedKeywords] 테스트 통과 - 표시 키워드: " + keywords.size() + "건");
        }

        @Test
        @DisplayName("삭제된 키워드는 제외한다")
        void findAll_shouldExcludeDeletedKeywords() {
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("활성").displayYn(Yn.Y).delYn(Yn.N).build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("삭제됨").displayYn(Yn.Y).delYn(Yn.Y).build());

            List<SearchKeyword> keywords = searchKeywordRepository
                    .findAllByDisplayYnAndDelYnOrderByPriorityAscIdDesc(Yn.Y, Yn.N);

            assertThat(keywords).hasSize(1);
            assertThat(keywords.get(0).getText()).isEqualTo("활성");
            System.out.println("✅ [findAll_shouldExcludeDeletedKeywords] 테스트 통과 - 활성 키워드: " + keywords.size() + "건");
        }
    }

    @Nested
    @DisplayName("searchKeywords 테스트")
    class SearchKeywordsTest {

        @Test
        @DisplayName("삭제 여부로 필터링한다")
        void searchKeywords_shouldFilterByDelYn() {
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("활성 키워드").delYn(Yn.N).build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("삭제된 키워드").delYn(Yn.Y).build());

            Pageable pageable = PageRequest.of(0, 10);
            Page<SearchKeyword> result = searchKeywordRepository.searchKeywords(
                    Yn.N, null, null, pageable
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getText()).isEqualTo("활성 키워드");
            System.out.println("✅ [searchKeywords_shouldFilterByDelYn] 테스트 통과 - 활성 키워드: " + result.getTotalElements() + "건");
        }

        @Test
        @DisplayName("표시 여부로 필터링한다")
        void searchKeywords_shouldFilterByDisplayYn() {
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("표시됨").displayYn(Yn.Y).build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("숨김").displayYn(Yn.N).build());

            Pageable pageable = PageRequest.of(0, 10);
            Page<SearchKeyword> result = searchKeywordRepository.searchKeywords(
                    Yn.N, Yn.Y, null, pageable
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getText()).isEqualTo("표시됨");
            System.out.println("✅ [searchKeywords_shouldFilterByDisplayYn] 테스트 통과 - 표시 키워드: " + result.getTotalElements() + "건");
        }

        @Test
        @DisplayName("키워드 포함 여부로 필터링한다")
        void searchKeywords_shouldFilterByKeywordContains() {
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("법안 검색").build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("국회 법안").build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("예산안").build());

            Pageable pageable = PageRequest.of(0, 10);
            Page<SearchKeyword> result = searchKeywordRepository.searchKeywords(
                    Yn.N, null, "법안", pageable
            );

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).extracting("text")
                    .containsExactlyInAnyOrder("법안 검색", "국회 법안");
            System.out.println("✅ [searchKeywords_shouldFilterByKeywordContains] 테스트 통과 - '법안' 포함: " + result.getTotalElements() + "건");
        }

        @Test
        @DisplayName("필터가 없으면 모든 활성 키워드를 반환한다")
        void searchKeywords_shouldReturnAllWhenNoFilters() {
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("키워드1").priority(2).build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("키워드2").priority(1).build());

            Pageable pageable = PageRequest.of(0, 10);
            Page<SearchKeyword> result = searchKeywordRepository.searchKeywords(
                    Yn.N, null, null, pageable
            );

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).getText()).isEqualTo("키워드2");
            assertThat(result.getContent().get(1).getText()).isEqualTo("키워드1");
            System.out.println("✅ [searchKeywords_shouldReturnAllWhenNoFilters] 테스트 통과 - 전체 조회: " + result.getTotalElements() + "건");
        }

        @Test
        @DisplayName("페이징 처리가 정상 작동한다")
        void searchKeywords_shouldReturnPagedResults() {
            for (int i = 1; i <= 15; i++) {
                searchKeywordRepository.save(SearchKeyword.builder()
                        .text("키워드" + i)
                        .priority(i)
                        .build());
            }

            Pageable pageable = PageRequest.of(1, 5);
            Page<SearchKeyword> result = searchKeywordRepository.searchKeywords(
                    Yn.N, null, null, pageable
            );

            assertThat(result.getTotalElements()).isEqualTo(15);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.getContent()).hasSize(5);
            assertThat(result.getNumber()).isEqualTo(1);
            System.out.println("✅ [searchKeywords_shouldReturnPagedResults] 테스트 통과 - 2페이지 조회: " + result.getContent().size() + "건");
        }
    }

    @Nested
    @DisplayName("existsByTextAndDelYn 테스트")
    class ExistsByTextAndDelYnTest {

        @Test
        @DisplayName("존재하는 키워드는 true를 반환한다")
        void existsByTextAndDelYn_shouldReturnTrueWhenExists() {
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("중복 체크").delYn(Yn.N).build());

            boolean exists = searchKeywordRepository.existsByTextAndDelYn("중복 체크", Yn.N);

            assertThat(exists).isTrue();
            System.out.println("✅ [existsByTextAndDelYn_shouldReturnTrueWhenExists] 테스트 통과 - 중복 키워드 감지");
        }

        @Test
        @DisplayName("삭제된 키워드는 false를 반환한다")
        void existsByTextAndDelYn_shouldReturnFalseWhenDeleted() {
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("삭제된 키워드").delYn(Yn.Y).build());

            boolean exists = searchKeywordRepository.existsByTextAndDelYn("삭제된 키워드", Yn.N);

            assertThat(exists).isFalse();
            System.out.println("✅ [existsByTextAndDelYn_shouldReturnFalseWhenDeleted] 테스트 통과 - 삭제된 키워드 제외");
        }

        @Test
        @DisplayName("존재하지 않는 키워드는 false를 반환한다")
        void existsByTextAndDelYn_shouldReturnFalseWhenNotFound() {
            boolean exists = searchKeywordRepository.existsByTextAndDelYn("없는 키워드", Yn.N);

            assertThat(exists).isFalse();
            System.out.println("✅ [existsByTextAndDelYn_shouldReturnFalseWhenNotFound] 테스트 통과 - 미존재 키워드");
        }
    }

    @Nested
    @DisplayName("countByDisplayYnAndDelYn 테스트")
    class CountByDisplayYnAndDelYnTest {

        @Test
        @DisplayName("표시된 키워드의 개수를 정확히 반환한다")
        void count_shouldReturnCorrectCountForDisplayedKeywords() {
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("표시1").displayYn(Yn.Y).delYn(Yn.N).build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("표시2").displayYn(Yn.Y).delYn(Yn.N).build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("숨김").displayYn(Yn.N).delYn(Yn.N).build());

            long count = searchKeywordRepository.countByDisplayYnAndDelYn(Yn.Y, Yn.N);

            assertThat(count).isEqualTo(2);
            System.out.println("✅ [count_shouldReturnCorrectCountForDisplayedKeywords] 테스트 통과 - 표시 키워드: " + count + "건");
        }

        @Test
        @DisplayName("삭제된 키워드는 제외한다")
        void count_shouldExcludeDeletedKeywords() {
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("활성").displayYn(Yn.Y).delYn(Yn.N).build());
            searchKeywordRepository.save(SearchKeyword.builder()
                    .text("삭제됨").displayYn(Yn.Y).delYn(Yn.Y).build());

            long count = searchKeywordRepository.countByDisplayYnAndDelYn(Yn.Y, Yn.N);

            assertThat(count).isEqualTo(1);
            System.out.println("✅ [count_shouldExcludeDeletedKeywords] 테스트 통과 - 활성 표시 키워드: " + count + "건");
        }
    }
}