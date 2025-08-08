package com.graydang.app.domain.comment.repository.projection;

import java.time.LocalDate;

public interface ReportedCommentSummaryProjection {

    Long getCommentId();
    String getCommentContent();
    Long getBillId();
    String getBillTitle();
    LocalDate getProposeDate();
    Long getCommenterUserId();
    String getCommenterEmail();
    String getCommenterNickname();
    int getReportedCount();

}
