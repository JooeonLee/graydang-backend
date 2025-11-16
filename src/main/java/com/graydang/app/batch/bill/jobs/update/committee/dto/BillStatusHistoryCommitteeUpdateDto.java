package com.graydang.app.batch.bill.jobs.update.committee.dto;

import com.graydang.app.batch.bill.dto.BillCommissionResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BillStatusHistoryCommitteeUpdateDto {

    /**
     * 'bill' 테이블의 기본 키 (id).
     * (Writer가 DB 작업을 할 때 사용할 PK)
     */
    private Long id; // bill.getId()

    /**
     * OpenAPI 의안 고유 ID (bill_id).
     * (Processor가 API 호출에 사용 + Writer가 로그 남길 때 사용)
     */
    private String billId; // bill.getBillId()

    /**
     * API 응답에서 파싱한 소관위 심사 정보.
     */
    private BillCommissionResponseDto.JurisdictionExaminationItem committeeItem;
}
