package com.graydang.app.domain.comment.service;

import com.graydang.app.domain.comment.model.dto.UserReportHistoryResponseDto;
import com.graydang.app.domain.comment.repository.CommentReportRepository;
import com.graydang.app.domain.comment.repository.projection.UserReportHistoryProjection;
import com.graydang.app.domain.user.service.UserService;
import com.graydang.app.global.common.model.dto.SliceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentReportServiceTest {

    @Mock
    private CommentReportRepository commentReportRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private CommentService commentService;
    
    @InjectMocks
    private CommentReportService commentReportService;
    
    @Nested
    @DisplayName("getUserReportHistory 메서드 테스트")
    class GetUserReportHistoryTest {
        
        @Test
        @DisplayName("특정 사용자의 신고 이력을 페이징하여 조회한다")
        void getUserReportHistory_shouldReturnPagedReportHistory() {
            // 2024-01-10 14:30:00
            LocalDateTime reportedAt1 = LocalDateTime.of(2024, 1, 10, 14, 30, 0);
            // 2024-01-09 10:15:00
            LocalDateTime reportedAt2 = LocalDateTime.of(2024, 1, 9, 10, 15, 0);
            
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            
            UserReportHistoryProjection projection1 = createProjection(
                    1L, "부적절한 내용", "PENDING", reportedAt1,
                    100L, "신고된 댓글 내용 1",
                    2L, "commenter1@example.com", "댓글작성자1",
                    1000L, "테스트 법안 1", LocalDate.of(2024, 1, 1)
            );
            
            UserReportHistoryProjection projection2 = createProjection(
                    2L, "욕설/비속어", "RESOLVED", reportedAt2,
                    101L, "신고된 댓글 내용 2",
                    3L, "commenter2@example.com", "댓글작성자2",
                    1001L, "테스트 법안 2", LocalDate.of(2024, 1, 2)
            );
            
            Slice<UserReportHistoryProjection> projectionSlice = 
                    new SliceImpl<>(List.of(projection1, projection2), pageable, true);
            
            when(userService.findByIdOrThrow(userId)).thenReturn(null);
            when(commentReportRepository.findByUserId(eq(userId), eq(pageable)))
                    .thenReturn(projectionSlice);
            
            SliceResponse<UserReportHistoryResponseDto> result = 
                    commentReportService.getUserReportHistory(userId, pageable);
            
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting("reportId", "reportReason", "reportStatus", "commentContent")
                    .containsExactly(
                            tuple(1L, "부적절한 내용", "PENDING", "신고된 댓글 내용 1"),
                            tuple(2L, "욕설/비속어", "RESOLVED", "신고된 댓글 내용 2")
                    );
            assertThat(result.isLast()).isFalse();
            
            System.out.println("✅ [getUserReportHistory_shouldReturnPagedReportHistory] 테스트 통과 - 신고 이력 조회: " + result.getContent().size() + "건");
            
            verify(userService).findByIdOrThrow(userId);
            verify(commentReportRepository).findByUserId(userId, pageable);
        }
        
        @Test
        @DisplayName("신고 이력이 없는 경우 빈 리스트를 반환한다")
        void getUserReportHistory_shouldReturnEmptyListWhenNoReports() {
            Long userId = 999L;
            Pageable pageable = PageRequest.of(0, 10);
            
            Slice<UserReportHistoryProjection> emptySlice = 
                    new SliceImpl<>(List.of(), pageable, false);
            
            when(userService.findByIdOrThrow(userId)).thenReturn(null);
            when(commentReportRepository.findByUserId(eq(userId), eq(pageable)))
                    .thenReturn(emptySlice);
            
            SliceResponse<UserReportHistoryResponseDto> result = 
                    commentReportService.getUserReportHistory(userId, pageable);
            
            assertThat(result.getContent()).isEmpty();
            assertThat(result.isLast()).isTrue();
            
            System.out.println("✅ [getUserReportHistory_shouldReturnEmptyListWhenNoReports] 테스트 통과 - 빈 결과 반환");
        }
        
        @Test
        @DisplayName("날짜 포맷팅이 올바르게 처리된다")
        void getUserReportHistory_shouldFormatDatesCorrectly() {
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            
            // 2024-03-15 09:45:30
            LocalDateTime reportedAt = LocalDateTime.of(2024, 3, 15, 9, 45, 30);
            // 2024-03-10
            LocalDate billProposeDate = LocalDate.of(2024, 3, 10);
            
            UserReportHistoryProjection projection = createProjection(
                    1L, "스팸", "PENDING", reportedAt,
                    100L, "댓글 내용",
                    2L, "user@example.com", "사용자",
                    1000L, "법안 제목", billProposeDate
            );
            
            Slice<UserReportHistoryProjection> projectionSlice = 
                    new SliceImpl<>(List.of(projection), pageable, false);
            
            when(userService.findByIdOrThrow(userId)).thenReturn(null);
            when(commentReportRepository.findByUserId(eq(userId), eq(pageable)))
                    .thenReturn(projectionSlice);
            
            SliceResponse<UserReportHistoryResponseDto> result = 
                    commentReportService.getUserReportHistory(userId, pageable);
            
            UserReportHistoryResponseDto dto = result.getContent().get(0);
            assertThat(dto.reportedAt()).isEqualTo("2024.03.15 09:45");
            assertThat(dto.billProposeDate()).isEqualTo("2024.03.10");
            
            System.out.println("✅ [getUserReportHistory_shouldFormatDatesCorrectly] 테스트 통과 - 날짜 포맷 확인");
        }
    }
    
    private UserReportHistoryProjection createProjection(
            Long reportId, String reportReason, String reportStatus, LocalDateTime reportedAt,
            Long commentId, String commentContent,
            Long commenterId, String commenterEmail, String commenterNickname,
            Long billId, String billTitle, LocalDate billProposeDate) {
        
        return new UserReportHistoryProjection() {
            @Override
            public Long getReportId() { return reportId; }
            @Override
            public String getReportReason() { return reportReason; }
            @Override
            public String getReportStatus() { return reportStatus; }
            @Override
            public LocalDateTime getReportedAt() { return reportedAt; }
            @Override
            public Long getCommentId() { return commentId; }
            @Override
            public String getCommentContent() { return commentContent; }
            @Override
            public Long getCommenterId() { return commenterId; }
            @Override
            public String getCommenterEmail() { return commenterEmail; }
            @Override
            public String getCommenterNickname() { return commenterNickname; }
            @Override
            public Long getBillId() { return billId; }
            @Override
            public String getBillTitle() { return billTitle; }
            @Override
            public LocalDate getBillProposeDate() { return billProposeDate; }
        };
    }
}