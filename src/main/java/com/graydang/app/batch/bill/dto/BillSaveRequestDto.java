package com.graydang.app.batch.bill.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@Getter
public class BillSaveRequestDto {
    private String billId;
    private String billName;
    private String proposeDate;
    private String representativeName;
    private String committeeName;
    private String summary;

    private String processResult;
    private String billStatus;
    private String status;

}
