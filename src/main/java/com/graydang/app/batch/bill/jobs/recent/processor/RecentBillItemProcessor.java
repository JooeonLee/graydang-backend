package com.graydang.app.batch.bill.jobs.recent.processor;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillReceiptInfoResponseDto;
import com.graydang.app.batch.bill.dto.BillRecentResponseDto;
import com.graydang.app.batch.bill.dto.BillSaveRequestDto;
import com.graydang.app.crawler.SummaryCrawler;
import com.graydang.app.domain.bill.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class RecentBillItemProcessor implements ItemProcessor<BillRecentResponseDto.ItemDto, BillSaveRequestDto> {

    private final BillRepository billRepository;
    private final BillApiClient billApiClient;
    private final XmlMapper xmlMapper;
    private final SummaryCrawler summaryCrawler;

    @Override
    public BillSaveRequestDto process(BillRecentResponseDto.ItemDto item) throws Exception {
        final String billId = item.getBillId();

        // 1. 중복 되는(이미 저장된) 의안이면 skip
        if (billRepository.existsByBillId(billId)) {
            log.debug("Skip existed bill: {}", billId);
            return null;
        }

        // 2. 의안접수정보조회 -> proposer, summaryLink
        var dtoOpt = billApiClient.getBillReceiptInfo(billId);
        if (dtoOpt.isEmpty()
                || dtoOpt.get().getBody() == null
                || dtoOpt.get().getBody().getItem() == null
                || dtoOpt.get().getBody().getItem().getReceipt() == null) {
            log.warn("No bill receipt found for billId: {}", billId);
            return null;
        }
        var receipt = dtoOpt.get().getBody().getItem().getReceipt();

        // 3. summaryLink 크롤링
        String summary = null;
        String summaryLink = receipt.getSummaryLink();
        if (summaryLink != null && !summaryLink.isBlank()) {
            try {
                // summary = summaryCrawler.extractSummary(receipt.getSummaryLink());
                summary = summaryCrawler.extractSummary(billId);

            } catch (Exception e) {
                log.warn("Failed to crawl summaryLink for billId: {}, link= {}", billId, summaryLink);
            }
        }

        // 4. 저장 dto 조합
        return BillSaveRequestDto.builder()
                .billId(billId)
                .billName(receipt.getBillName())
                .proposeDate(receipt.getProposeDt())
                .representativeName(receipt.getProposer())
                .committeeName(item.getCommitteeName())
                .summary(summary)

                .processResult("접수")
                .billStatus("접수")
                .status("ACTIVE")
                .build();
    }
}
