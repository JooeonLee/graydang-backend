package com.graydang.app.batch.bill.jobs.committee.dto;

import com.graydang.app.batch.bill.dto.BillCommissionResponseDto;

public record BillUpdateDto(
        String billId,
        BillCommissionResponseDto.JurisdictionExaminationItem item
) {
}
