package com.graydang.app.batch.bill.jobs.summarize.processor;

import com.graydang.app.batch.bill.client.GptApiClient;
import com.graydang.app.batch.bill.dto.BillGptSummaryDto;
import com.graydang.app.domain.bill.model.Bill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummarizeBillItemProcessor implements ItemProcessor<Bill, Bill> {

    private final GptApiClient gptApiClient;

    @Override
    public Bill process(Bill bill) {
        log.info("Starting AI summary for Bill ID: {}", bill.getBillId());

        String originalTitle = bill.getTitle();
        String originalSummary = bill.getSummary();

        if (originalSummary == null || originalSummary.isBlank()) {
            log.warn("Original summary is empty for Bill ID: {}. Skipping.", bill.getBillId());
            return null;

        }

        try {
            // [변경] 실제 GptApiClient를 호출하여 AI 요약 결과를 받아옵니다.
            BillGptSummaryDto gptResult = gptApiClient.generateTitleAndSummary(originalTitle, originalSummary);

            String aiTitle = gptResult.getTitle();
            String aiSummary = gptResult.getSummary();

            // 엔티티의 update 메서드를 사용해 AI 요약 결과와 처리 상태를 업데이트합니다.
            bill.updateAiSummary(aiTitle, aiSummary);

            log.info("Successfully generated AI summary for Bill ID: {}", bill.getBillId());

            return bill;

        } catch (Exception e) {
            // GptApiClient에서 RuntimeException이 발생하면 여기서 처리됩니다.
            log.error("Failed to generate AI summary for Bill ID: {}. Error: {}", bill.getBillId(), e.getMessage());
            // 실패한 아이템은 null을 반환하여 Writer로 넘어가지 않도록 합니다.
            return null;
        }
    }
}
