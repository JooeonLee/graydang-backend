package com.graydang.app.domain.comment.model.dto;

import com.graydang.app.domain.comment.repository.projection.UserReportHistoryProjection;
import lombok.Builder;

import java.time.format.DateTimeFormatter;

@Builder
public record UserReportHistoryResponseDto(
    Long reportId,
    String reportReason,
    String reportStatus,
    String reportedAt,
    
    Long commentId,
    String commentContent,
    
    Long commenterId,
    String commenterEmail,
    String commenterNickname,
    
    Long billId,
    String billTitle,
    String billProposeDate
) {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    
    public static UserReportHistoryResponseDto from(UserReportHistoryProjection projection) {
        return UserReportHistoryResponseDto.builder()
                .reportId(projection.getReportId())
                .reportReason(projection.getReportReason())
                .reportStatus(projection.getReportStatus())
                .reportedAt(projection.getReportedAt() != null ? 
                        projection.getReportedAt().format(DATE_TIME_FORMATTER) : "")
                
                .commentId(projection.getCommentId())
                .commentContent(projection.getCommentContent())
                
                .commenterId(projection.getCommenterId())
                .commenterEmail(projection.getCommenterEmail())
                .commenterNickname(projection.getCommenterNickname())
                
                .billId(projection.getBillId())
                .billTitle(projection.getBillTitle())
                .billProposeDate(projection.getBillProposeDate() != null ? 
                        projection.getBillProposeDate().format(DATE_FORMATTER) : "")
                .build();
    }
}