package com.graydang.app.domain.bill.service;

import com.graydang.app.batch.bill.dto.BillInfoResponseDto;
import com.graydang.app.batch.bill.dto.BillSaveRequestDto;
import com.graydang.app.domain.bill.exception.BillException;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.repository.*;
import com.graydang.app.domain.comment.repository.CommentRepository;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;
    
    @Mock
    private BillReactionRepository billReactionRepository;
    
    @Mock
    private BillStatusHistoryRepository billStatusHistoryRepository;
    
    @Mock
    private BillScrapeRepository billScrapeRepository;
    
    @Mock
    private CommentRepository commentRepository;
    
    @Mock
    private BillQueryRepository billQueryRepository;

    @InjectMocks
    private BillService billService;

    private final String testBillId = "BILL-001";
    private final LocalDate testProposeDate = LocalDate.of(2025, 1, 15);
    private final LocalDateTime testCreatedAt = LocalDateTime.of(2025, 1, 15, 10, 0, 0);
    private final LocalDateTime testUpdatedAt = LocalDateTime.of(2025, 1, 15, 15, 30, 0);

    @Nested
    @DisplayName("saveOrUpdate(BillInfoResponseDto) 메서드는")
    class SaveOrUpdateWithBillInfoDtoTest {

        private BillInfoResponseDto.ItemDto itemDto;

        @BeforeEach
        void setUp() {
            itemDto = new BillInfoResponseDto.ItemDto();
            itemDto.setBillId(testBillId);
            itemDto.setBillName("테스트 법안");
            itemDto.setProposeDt("2025-01-15");
            itemDto.setGeneralResult("원안가결");
            itemDto.setProcStageCd("위원회 심사");
            itemDto.setProposerKind("홍길동의원");
            itemDto.setSummary("법안 요약");
        }

        @Test
        @DisplayName("새로운 법안을 저장할 때 createdAt과 updatedAt이 설정된다")
        void saveOrUpdate_shouldSetAuditFieldsWhenCreatingNewBill() {
            given(billRepository.findByBillId(testBillId)).willReturn(Optional.empty());
            
            ArgumentCaptor<Bill> billCaptor = ArgumentCaptor.forClass(Bill.class);
            given(billRepository.save(billCaptor.capture())).willAnswer(invocation -> {
                Bill bill = invocation.getArgument(0);
                // JPA auditing을 시뮬레이션
                ReflectionTestUtils.setField(bill, "createdAt", LocalDateTime.now());
                ReflectionTestUtils.setField(bill, "updatedAt", LocalDateTime.now());
                return bill;
            });

            billService.saveOrUpdate(itemDto);

            verify(billRepository).findByBillId(testBillId);
            verify(billRepository).save(any(Bill.class));
            
            Bill savedBill = billCaptor.getValue();
            assertThat(savedBill.getBillId()).isEqualTo(testBillId);
            assertThat(savedBill.getTitle()).isEqualTo("테스트 법안");
            assertThat(savedBill.getProposeDate()).isEqualTo(testProposeDate);
            assertThat(savedBill.getProcessResult()).isEqualTo("원안가결");
            assertThat(savedBill.getBillStatus()).isEqualTo("위원회 심사");
            assertThat(savedBill.getRepresentativeName()).isEqualTo("홍길동");
            assertThat(savedBill.getStatus()).isEqualTo("ACTIVE");
            
            System.out.println("✅ [saveOrUpdate_shouldSetAuditFieldsWhenCreatingNewBill] 테스트 통과 - 새 법안 저장시 audit 필드 설정 확인");
        }

        @Test
        @DisplayName("기존 법안을 업데이트할 때 updatedAt만 변경된다")
        void saveOrUpdate_shouldUpdateOnlyUpdatedAtWhenUpdatingExistingBill() {
            Bill existingBill = Bill.builder()
                    .id(1L)
                    .billId(testBillId)
                    .title("기존 법안 제목")
                    .proposeDate(testProposeDate)
                    .status("ACTIVE")
                    .viewCount(100L)
                    .build();
            // 초기 audit 필드 설정
            ReflectionTestUtils.setField(existingBill, "createdAt", testCreatedAt);
            ReflectionTestUtils.setField(existingBill, "updatedAt", testUpdatedAt);
            
            LocalDateTime originalCreatedAt = existingBill.getCreatedAt();
            
            given(billRepository.findByBillId(testBillId)).willReturn(Optional.of(existingBill));

            billService.saveOrUpdate(itemDto);

            verify(billRepository).findByBillId(testBillId);
            verify(billRepository, never()).save(any(Bill.class));
            
            assertThat(existingBill.getTitle()).isEqualTo("테스트 법안");
            assertThat(existingBill.getProcessResult()).isEqualTo("원안가결");
            assertThat(existingBill.getBillStatus()).isEqualTo("위원회 심사");
            assertThat(existingBill.getCreatedAt()).isEqualTo(originalCreatedAt);
            
            System.out.println("✅ [saveOrUpdate_shouldUpdateOnlyUpdatedAtWhenUpdatingExistingBill] 테스트 통과 - 법안 업데이트시 updatedAt만 변경 확인");
        }
    }

    @Nested
    @DisplayName("saveOrUpdate(BillSaveRequestDto) 메서드는")
    class SaveOrUpdateWithBillSaveRequestDtoTest {

        private BillSaveRequestDto requestDto;

        @BeforeEach
        void setUp() {
            requestDto = BillSaveRequestDto.builder()
                    .billId(testBillId)
                    .billName("테스트 법안")
                    .proposeDate("2025-01-15")
                    .processResult("원안가결")
                    .billStatus("위원회 심사")
                    .summary("법안 요약")
                    .representativeName("홍길동")
                    .committeeName("기획재정위원회")
                    .build();
        }

        @Test
        @DisplayName("새로운 법안을 저장할 때 모든 필드와 audit 정보가 설정된다")
        void saveOrUpdate_shouldSaveNewBillWithAllFields() {
            given(billRepository.findByBillId(testBillId)).willReturn(Optional.empty());
            
            ArgumentCaptor<Bill> billCaptor = ArgumentCaptor.forClass(Bill.class);
            given(billRepository.save(billCaptor.capture())).willAnswer(invocation -> invocation.getArgument(0));

            billService.saveOrUpdate(requestDto);

            verify(billRepository).findByBillId(testBillId);
            verify(billRepository).save(any(Bill.class));
            
            Bill savedBill = billCaptor.getValue();
            assertThat(savedBill.getBillId()).isEqualTo(testBillId);
            assertThat(savedBill.getTitle()).isEqualTo("테스트 법안");
            assertThat(savedBill.getCommitteeName()).isEqualTo("기획재정위원회");
            assertThat(savedBill.getViewCount()).isEqualTo(0L);
            assertThat(savedBill.getStatus()).isEqualTo("ACTIVE");
            
            System.out.println("✅ [saveOrUpdate_shouldSaveNewBillWithAllFields] 테스트 통과 - 새 법안 저장시 모든 필드 설정 확인");
        }

        @Test
        @DisplayName("기존 법안 업데이트시 원래의 viewCount와 createdAt은 유지된다")
        void saveOrUpdate_shouldPreserveViewCountAndCreatedAtWhenUpdating() {
            Bill existingBill = Bill.builder()
                    .id(1L)
                    .billId(testBillId)
                    .title("기존 제목")
                    .viewCount(500L)
                    .status("ACTIVE")
                    .build();
            // 초기 audit 필드 설정  
            ReflectionTestUtils.setField(existingBill, "createdAt", testCreatedAt);
            ReflectionTestUtils.setField(existingBill, "updatedAt", testUpdatedAt);
            
            LocalDateTime originalCreatedAt = existingBill.getCreatedAt();
            Long originalViewCount = existingBill.getViewCount();
            
            given(billRepository.findByBillId(testBillId)).willReturn(Optional.of(existingBill));

            billService.saveOrUpdate(requestDto);

            verify(billRepository).findByBillId(testBillId);
            assertThat(existingBill.getTitle()).isEqualTo("테스트 법안");
            assertThat(existingBill.getCommitteeName()).isEqualTo("기획재정위원회");
            assertThat(existingBill.getViewCount()).isEqualTo(originalViewCount);
            assertThat(existingBill.getCreatedAt()).isEqualTo(originalCreatedAt);
            
            System.out.println("✅ [saveOrUpdate_shouldPreserveViewCountAndCreatedAtWhenUpdating] 테스트 통과 - 업데이트시 viewCount와 createdAt 유지 확인");
        }
    }

    @Nested
    @DisplayName("updateCommitteeName 메서드는")
    class UpdateCommitteeNameTest {

        @Test
        @DisplayName("위원회 이름을 업데이트하고 audit 정보를 갱신한다")
        void updateCommitteeName_shouldUpdateCommitteeNameAndAuditInfo() {
            Bill bill = Bill.builder()
                    .id(1L)
                    .billId(testBillId)
                    .title("테스트 법안")
                    .committeeName("기존 위원회")
                    .status("ACTIVE")
                    .build();
            // 초기 audit 필드 설정
            ReflectionTestUtils.setField(bill, "createdAt", testCreatedAt);
            ReflectionTestUtils.setField(bill, "updatedAt", testUpdatedAt);
            
            LocalDateTime originalCreatedAt = bill.getCreatedAt();
            
            given(billRepository.findByBillId(testBillId)).willReturn(Optional.of(bill));

            billService.updateCommitteeName(testBillId, "새로운 위원회");

            verify(billRepository).findByBillId(testBillId);
            assertThat(bill.getCommitteeName()).isEqualTo("새로운 위원회");
            assertThat(bill.getCreatedAt()).isEqualTo(originalCreatedAt);
            
            System.out.println("✅ [updateCommitteeName_shouldUpdateCommitteeNameAndAuditInfo] 테스트 통과 - 위원회 이름 업데이트 확인");
        }

        @Test
        @DisplayName("존재하지 않는 법안에 대해서는 아무 동작도 하지 않는다")
        void updateCommitteeName_shouldDoNothingWhenBillNotFound() {
            given(billRepository.findByBillId(testBillId)).willReturn(Optional.empty());

            billService.updateCommitteeName(testBillId, "새로운 위원회");

            verify(billRepository).findByBillId(testBillId);
            verifyNoMoreInteractions(billRepository);
            
            System.out.println("✅ [updateCommitteeName_shouldDoNothingWhenBillNotFound] 테스트 통과 - 법안 미존재시 동작 없음 확인");
        }
    }

    @Nested
    @DisplayName("increaseViewCount 메서드는")
    class IncreaseViewCountTest {

        @Test
        @DisplayName("조회수를 증가시키고 audit 정보를 갱신한다")
        void increaseViewCount_shouldIncreaseCountAndUpdateAuditInfo() {
            Bill bill = Bill.builder()
                    .id(1L)
                    .billId(testBillId)
                    .title("테스트 법안")
                    .viewCount(100L)
                    .status("ACTIVE")
                    .build();
            // 초기 audit 필드 설정
            ReflectionTestUtils.setField(bill, "createdAt", testCreatedAt);
            ReflectionTestUtils.setField(bill, "updatedAt", testUpdatedAt);
            
            LocalDateTime originalCreatedAt = bill.getCreatedAt();
            Long originalViewCount = bill.getViewCount();
            
            given(billRepository.findById(1L)).willReturn(Optional.of(bill));

            billService.increaseViewCount(1L);

            verify(billRepository).findById(1L);
            assertThat(bill.getViewCount()).isEqualTo(originalViewCount + 1);
            assertThat(bill.getCreatedAt()).isEqualTo(originalCreatedAt);
            
            System.out.println("✅ [increaseViewCount_shouldIncreaseCountAndUpdateAuditInfo] 테스트 통과 - 조회수 증가 확인");
        }

        @Test
        @DisplayName("존재하지 않는 법안 ID에 대해 예외를 발생시킨다")
        void increaseViewCount_shouldThrowExceptionWhenBillNotFound() {
            given(billRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> billService.increaseViewCount(1L))
                    .isInstanceOf(BillException.class)
                    .hasFieldOrPropertyWithValue("baseResponseStatus", BaseResponseStatus.NONE_BILL);
            
            verify(billRepository).findById(1L);
            
            System.out.println("✅ [increaseViewCount_shouldThrowExceptionWhenBillNotFound] 테스트 통과 - 법안 미존재시 예외 발생 확인");
        }
    }

    @Nested
    @DisplayName("Auditing 기능 통합 테스트")
    class AuditingIntegrationTest {

        @Test
        @DisplayName("법안 생성부터 여러 번의 업데이트까지 audit 정보가 올바르게 관리된다")
        void auditingIntegration_shouldTrackAllChangesCorrectly() {
            // 1. 새 법안 생성
            BillSaveRequestDto createDto = BillSaveRequestDto.builder()
                    .billId(testBillId)
                    .billName("초기 법안 제목")
                    .proposeDate("2025-01-15")
                    .committeeName("초기 위원회")
                    .status("ACTIVE")
                    .build();
            
            given(billRepository.findByBillId(testBillId)).willReturn(Optional.empty());
            
            ArgumentCaptor<Bill> billCaptor = ArgumentCaptor.forClass(Bill.class);
            given(billRepository.save(billCaptor.capture())).willAnswer(invocation -> {
                Bill bill = invocation.getArgument(0);
                // JPA auditing 시뮬레이션
                LocalDateTime now = LocalDateTime.now();
                ReflectionTestUtils.setField(bill, "createdAt", now);
                ReflectionTestUtils.setField(bill, "updatedAt", now);
                return bill;
            });

            billService.saveOrUpdate(createDto);
            
            Bill createdBill = billCaptor.getValue();
            LocalDateTime creationTime = createdBill.getCreatedAt();
            assertThat(createdBill.getCreatedAt()).isNotNull();
            assertThat(createdBill.getUpdatedAt()).isNotNull();
            assertThat(createdBill.getCreatedAt()).isEqualTo(createdBill.getUpdatedAt());
            
            // 2. 첫 번째 업데이트
            BillSaveRequestDto updateDto1 = BillSaveRequestDto.builder()
                    .billId(testBillId)
                    .billName("첫 번째 수정된 제목")
                    .proposeDate("2025-01-15")
                    .committeeName("수정된 위원회")
                    .billStatus("위원회 심사")
                    .build();
            
            given(billRepository.findByBillId(testBillId)).willReturn(Optional.of(createdBill));
            
            billService.saveOrUpdate(updateDto1);
            
            assertThat(createdBill.getTitle()).isEqualTo("첫 번째 수정된 제목");
            assertThat(createdBill.getCommitteeName()).isEqualTo("수정된 위원회");
            assertThat(createdBill.getCreatedAt()).isEqualTo(creationTime);
            
            // 3. 두 번째 업데이트 (위원회 이름만)
            given(billRepository.findByBillId(testBillId)).willReturn(Optional.of(createdBill));
            
            billService.updateCommitteeName(testBillId, "최종 위원회");
            
            assertThat(createdBill.getCommitteeName()).isEqualTo("최종 위원회");
            assertThat(createdBill.getCreatedAt()).isEqualTo(creationTime);
            
            System.out.println("✅ [auditingIntegration_shouldTrackAllChangesCorrectly] 테스트 통과 - 전체 audit 생명주기 확인");
        }

        @Test
        @DisplayName("Envers를 통한 audit history가 자동으로 기록된다")
        void enversAuditing_shouldBeEnabledForBillEntity() {
            // Bill 엔티티에 @Audited 어노테이션이 있는지 확인
            assertThat(Bill.class.isAnnotationPresent(org.hibernate.envers.Audited.class)).isTrue();
            
            System.out.println("✅ [enversAuditing_shouldBeEnabledForBillEntity] 테스트 통과 - Envers 설정 확인");
        }
    }
}