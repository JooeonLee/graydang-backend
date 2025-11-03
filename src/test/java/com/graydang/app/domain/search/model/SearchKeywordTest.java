package com.graydang.app.domain.search.model;

import com.graydang.app.domain.common.Yn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchKeywordTest {

    @Nested
    @DisplayName("SearchKeyword 빌더 테스트")
    class SearchKeywordBuilderTest {

        @Test
        @DisplayName("기본값으로 생성된다")
        void builder_shouldCreateWithDefaultValues() {
            SearchKeyword searchKeyword = SearchKeyword.builder()
                    .text("테스트 키워드")
                    .build();

            assertThat(searchKeyword.getText()).isEqualTo("테스트 키워드");
            assertThat(searchKeyword.getPriority()).isEqualTo(0);
            assertThat(searchKeyword.getDisplayYn()).isEqualTo(Yn.Y);
            assertThat(searchKeyword.getDelYn()).isEqualTo(Yn.N);
            System.out.println("✅ [builder_shouldCreateWithDefaultValues] 테스트 통과 - 기본값 검증 완료");
        }

        @Test
        @DisplayName("커스텀 값으로 생성된다")
        void builder_shouldCreateWithCustomValues() {
            SearchKeyword searchKeyword = SearchKeyword.builder()
                    .text("커스텀 키워드")
                    .priority(5)
                    .displayYn(Yn.N)
                    .delYn(Yn.Y)
                    .build();

            assertThat(searchKeyword.getText()).isEqualTo("커스텀 키워드");
            assertThat(searchKeyword.getPriority()).isEqualTo(5);
            assertThat(searchKeyword.getDisplayYn()).isEqualTo(Yn.N);
            assertThat(searchKeyword.getDelYn()).isEqualTo(Yn.Y);
            System.out.println("✅ [builder_shouldCreateWithCustomValues] 테스트 통과 - 커스텀값 검증 완료");
        }
    }

    @Nested
    @DisplayName("표시 상태 변경 테스트")
    class DisplayStateTest {

        @Test
        @DisplayName("show 메서드는 displayYn을 Y로 변경한다")
        void show_shouldChangeDisplayYnToY() {
            SearchKeyword searchKeyword = SearchKeyword.builder()
                    .text("테스트")
                    .displayYn(Yn.N)
                    .build();

            searchKeyword.show();

            assertThat(searchKeyword.getDisplayYn()).isEqualTo(Yn.Y);
            System.out.println("✅ [show_shouldChangeDisplayYnToY] 테스트 통과 - 표시 상태 변경 확인");
        }

        @Test
        @DisplayName("hide 메서드는 displayYn을 N으로 변경한다")
        void hide_shouldChangeDisplayYnToN() {
            SearchKeyword searchKeyword = SearchKeyword.builder()
                    .text("테스트")
                    .displayYn(Yn.Y)
                    .build();

            searchKeyword.hide();

            assertThat(searchKeyword.getDisplayYn()).isEqualTo(Yn.N);
            System.out.println("✅ [hide_shouldChangeDisplayYnToN] 테스트 통과 - 비표시 상태 변경 확인");
        }
    }

    @Nested
    @DisplayName("소프트 삭제 테스트")
    class SoftDeleteTest {

        @Test
        @DisplayName("softDelete 메서드는 delYn을 Y로 변경한다")
        void softDelete_shouldChangeDelYnToY() {
            SearchKeyword searchKeyword = SearchKeyword.builder()
                    .text("테스트")
                    .delYn(Yn.N)
                    .build();

            searchKeyword.softDelete();

            assertThat(searchKeyword.getDelYn()).isEqualTo(Yn.Y);
            System.out.println("✅ [softDelete_shouldChangeDelYnToY] 테스트 통과 - 소프트 삭제 확인");
        }

        @Test
        @DisplayName("restore 메서드는 delYn을 N으로 변경한다")
        void restore_shouldChangeDelYnToN() {
            SearchKeyword searchKeyword = SearchKeyword.builder()
                    .text("테스트")
                    .delYn(Yn.Y)
                    .build();

            searchKeyword.restore();

            assertThat(searchKeyword.getDelYn()).isEqualTo(Yn.N);
            System.out.println("✅ [restore_shouldChangeDelYnToN] 테스트 통과 - 복구 확인");
        }
    }

    @Nested
    @DisplayName("속성 변경 테스트")
    class PropertyChangeTest {

        @Test
        @DisplayName("changePriority 메서드는 우선순위를 업데이트한다")
        void changePriority_shouldUpdatePriority() {
            SearchKeyword searchKeyword = SearchKeyword.builder()
                    .text("테스트")
                    .priority(1)
                    .build();

            searchKeyword.changePriority(10);

            assertThat(searchKeyword.getPriority()).isEqualTo(10);
            System.out.println("✅ [changePriority_shouldUpdatePriority] 테스트 통과 - 우선순위 변경: 10");
        }

        @Test
        @DisplayName("changeText 메서드는 텍스트를 업데이트한다")
        void changeText_shouldUpdateText() {
            SearchKeyword searchKeyword = SearchKeyword.builder()
                    .text("원본 텍스트")
                    .build();

            searchKeyword.changeText("변경된 텍스트");

            assertThat(searchKeyword.getText()).isEqualTo("변경된 텍스트");
            System.out.println("✅ [changeText_shouldUpdateText] 테스트 통과 - 텍스트 변경 확인");
        }
    }
}