package com.graydang.app.domain.comment.model.dto;

import com.graydang.app.domain.comment.repository.projection.ReportedCommentSummaryProjection;
import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record ReportedCommentSummaryResponseDto(

    Long commentId,
    String commentContent,

    Long billId,
    String billTitle,
    String proposeDate,

    Long commenterUserId,
    String commenterEmail,
    String commenterNickname,

    int reportedCount
) {
    public static ReportedCommentSummaryResponseDto from(ReportedCommentSummaryProjection projection) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        return new ReportedCommentSummaryResponseDto(
                projection.getCommentId(),
                projection.getCommentContent(),

                projection.getBillId(),
                projection.getBillTitle(),
                projection.getProposeDate() != null ? projection.getProposeDate().format(formatter) : "",

                projection.getCommenterUserId(),
                projection.getCommenterEmail(),
                projection.getCommenterNickname(),

                projection.getReportedCount()
        );
    }
}
