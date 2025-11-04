package com.graydang.app.batch.bill.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillPassageResult {
    private int totalFetchedCount;  // API에서 조회한 전체 건수
    private int todayPassedCount;   // 오늘 날짜에 해당하는 건수
    private List<BillRecentPassageResponseDto.ItemDto> todayPassedBills;  // 오늘 통과된 법안 목록
}