package com.graydang.app.domain.comment.repository.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface UserReportHistoryProjection {
    Long getReportId();
    String getReportReason();
    String getReportStatus();
    LocalDateTime getReportedAt();
    
    Long getCommentId();
    String getCommentContent();
    
    Long getCommenterId();
    String getCommenterEmail();
    String getCommenterNickname();
    
    Long getBillId();
    String getBillTitle();
    LocalDate getBillProposeDate();
}