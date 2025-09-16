package com.graydang.app.batch.bill.jobs.committee.processor;

import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillCommissionResponseDto;
import com.graydang.app.batch.bill.jobs.committee.dto.BillUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillCommitteeItemProcessor implements ItemProcessor<String, BillUpdateDto> {

    private final BillApiClient billApiClient;
    private static final int SLEEP_MS = 50;

    @Override
    public BillUpdateDto process(String billId) throws Exception {
        // API 과부하를 줄이기 위한 지연 시간 (Rate Limiting)
        Thread.sleep(SLEEP_MS);

        try {
            // 1. API를 호출하고 응답을 Optional 변수에 저장
            Optional<BillCommissionResponseDto> responseOptional = billApiClient.getBillCommissionInfo(billId);

            // 2. API 호출은 성공했지만, 응답 데이터가 비어있는 경우를 명확히 로깅
            if (responseOptional.isEmpty()) {
                log.warn("API call for billId [{}] was successful, but returned no data.", billId);
                return null;
            }

            // DTO의 편의 메서드를 사용해 원하는 데이터를 추출
            BillCommissionResponseDto response = responseOptional.get();
            Optional<BillCommissionResponseDto.JurisdictionExaminationItem> itemOptional = response.getFirstJurisdictionExaminationItem();

            // Step 3: 데이터 유무에 따라 분기 처리
            if (itemOptional.isPresent()) {
                // 데이터가 존재하면 Writer로 넘길 DTO를 생성하여 반환합니다.
                BillCommissionResponseDto.JurisdictionExaminationItem item = itemOptional.get();
                return new BillUpdateDto(billId, item);
            } else {
                // 데이터가 존재하지 않으면 로그를 남기고 null을 반환하여 필터링합니다.
                log.warn("API data received for billId [{}], but the required 'Jurisdiction Examination Item' was not found.", billId);
                return null;
            }

        } catch (Exception e) {
            // Step 4: API 호출 중 예외 발생 시 처리
            log.error("❌ Exception during API call for billId [{}]. Error: {}", billId, e.getMessage(), e);
            return null;
        }
    }
}
