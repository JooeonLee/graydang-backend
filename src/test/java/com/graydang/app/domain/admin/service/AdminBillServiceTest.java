package com.graydang.app.domain.admin.service;

import com.graydang.app.domain.admin.model.dto.BillUpdateRequestDto;
import com.graydang.app.domain.bill.exception.BillException;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.repository.BillRepository;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminBillServiceTest {

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private AdminBillService adminBillService;

    private Bill testBill;
    private final Long testBillId = 1L;
    private final Long adminId = 999L;

    @BeforeEach
    void setUp() {
        testBill = Bill.builder()
                .id(testBillId)
                .billId("BILL-001")
                .title("기존 법안 제목")
                .summary("기존 법안 요약")
                .aiTitle("기존 AI 제목")
                .aiSummary("기존 AI 요약")
                .committeeName("기획재정위원회")
                .billStatus("위원회 심사")
                .proposeDate(LocalDate.of(2025, 1, 1))
                .status("ACTIVE")
                .build();
    }

    @Nested
    @DisplayName("updateBill 메서드는")
    class UpdateBillTest {

        @Test
        @DisplayName("모든 필드를 업데이트한다")
        void updateBill_shouldUpdateAllFields() {
            BillUpdateRequestDto requestDto = BillUpdateRequestDto.builder()
                    .title("수정된 법안 제목")
                    .summary("수정된 법안 요약")
                    .aiTitle("수정된 AI 제목")
                    .aiSummary("수정된 AI 요약")
                    .committeeName("교육위원회")
                    .billStatus("본회의 심사")
                    .build();

            given(billRepository.findById(testBillId)).willReturn(Optional.of(testBill));

            adminBillService.updateBill(testBillId, requestDto, adminId);

            verify(billRepository).findById(testBillId);
            assertThat(testBill.getTitle()).isEqualTo("수정된 법안 제목");
            assertThat(testBill.getSummary()).isEqualTo("수정된 법안 요약");
            assertThat(testBill.getAiTitle()).isEqualTo("수정된 AI 제목");
            assertThat(testBill.getAiSummary()).isEqualTo("수정된 AI 요약");
            assertThat(testBill.getCommitteeName()).isEqualTo("교육위원회");
            assertThat(testBill.getBillStatus()).isEqualTo("본회의 심사");
            System.out.println("✅ [updateBill_shouldUpdateAllFields] 테스트 통과 - 모든 필드 업데이트 확인");
        }

        @Test
        @DisplayName("일부 필드만 업데이트한다")
        void updateBill_shouldUpdatePartialFields() {
            BillUpdateRequestDto requestDto = BillUpdateRequestDto.builder()
                    .title("부분 수정된 제목")
                    .billStatus("가결")
                    .build();

            given(billRepository.findById(testBillId)).willReturn(Optional.of(testBill));

            adminBillService.updateBill(testBillId, requestDto, adminId);

            verify(billRepository).findById(testBillId);
            assertThat(testBill.getTitle()).isEqualTo("부분 수정된 제목");
            assertThat(testBill.getBillStatus()).isEqualTo("가결");
            assertThat(testBill.getSummary()).isEqualTo("기존 법안 요약");
            assertThat(testBill.getCommitteeName()).isEqualTo("기획재정위원회");
            System.out.println("✅ [updateBill_shouldUpdatePartialFields] 테스트 통과 - 일부 필드만 업데이트 확인");
        }

        @Test
        @DisplayName("존재하지 않는 법안 ID로 업데이트 시도시 예외를 발생시킨다")
        void updateBill_shouldThrowExceptionWhenBillNotFound() {
            BillUpdateRequestDto requestDto = BillUpdateRequestDto.builder()
                    .title("수정 시도")
                    .build();

            given(billRepository.findById(testBillId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminBillService.updateBill(testBillId, requestDto, adminId))
                    .isInstanceOf(BillException.class)
                    .hasFieldOrPropertyWithValue("baseResponseStatus", BaseResponseStatus.NONE_BILL);
            
            verify(billRepository).findById(testBillId);
            System.out.println("✅ [updateBill_shouldThrowExceptionWhenBillNotFound] 테스트 통과 - 법안 미존재시 예외 발생 확인");
        }

        @Test
        @DisplayName("빈 문자열이나 null 값은 업데이트하지 않는다")
        void updateBill_shouldIgnoreEmptyAndNullValues() {
            BillUpdateRequestDto requestDto = BillUpdateRequestDto.builder()
                    .title("")
                    .summary("   ")
                    .aiTitle(null)
                    .aiSummary("")
                    .committeeName("   ")
                    .billStatus("수정된 상태")
                    .build();

            given(billRepository.findById(testBillId)).willReturn(Optional.of(testBill));

            adminBillService.updateBill(testBillId, requestDto, adminId);

            verify(billRepository).findById(testBillId);
            assertThat(testBill.getTitle()).isEqualTo("기존 법안 제목");
            assertThat(testBill.getSummary()).isEqualTo("기존 법안 요약");
            assertThat(testBill.getAiTitle()).isEqualTo("기존 AI 제목");
            assertThat(testBill.getAiSummary()).isEqualTo("기존 AI 요약");
            assertThat(testBill.getCommitteeName()).isEqualTo("기획재정위원회");
            assertThat(testBill.getBillStatus()).isEqualTo("수정된 상태");
            System.out.println("✅ [updateBill_shouldIgnoreEmptyAndNullValues] 테스트 통과 - 빈 값 무시 확인");
        }
    }
}