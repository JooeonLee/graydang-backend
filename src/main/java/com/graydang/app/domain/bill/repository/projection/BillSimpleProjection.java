package com.graydang.app.domain.bill.repository.projection;

public interface BillSimpleProjection {

    Long getBillId();
    String getAiTitle();
    String getRepresentativeName();
    String getProposeDate();
    String getBillHistoryStatus();
    String getCommitteeName();
    long getViewCount();
    long getReactionCount();
    long getCommentCount();
    boolean getScraped();
}
