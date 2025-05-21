package com.graydang.app.batch.bill.tasklet;

import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillTransferredInfoResponseDto;
import com.graydang.app.domain.bill.repository.BillRepository;
import com.graydang.app.domain.bill.service.BillService;
import com.graydang.app.domain.bill.service.BillStatusHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
@Component
public class BillGovTransferredProcessingTasklet implements Tasklet {

    private final BillApiClient billApiClient;
    private final BillService billService;
    private final BillRepository billRepository;
    private final BillStatusHistoryService billStatusHistoryService;

    private static final int PAGE_SIZE = 50;
    private static final int MAX_COUNT = 500;
    private static final int SLEEP_MS = 100;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        AtomicInteger updated = new AtomicInteger();
        int triedCount = 0;
        int page = 0;

        while (triedCount < MAX_COUNT) {
            Page<String> billPage = billRepository.findLatestBillIds(PageRequest.of(page, PAGE_SIZE));
            List<String> billIds = billPage.getContent();

            if (billIds.isEmpty()) break;

            for (String billId : billIds) {
                try {
                    billApiClient.getBillTransferredInfo(billId).ifPresent(dto -> {
                        List<BillTransferredInfoResponseDto.TransferredItem> items = dto.getBody().getItems();

                        if (items != null && !items.isEmpty()) {
                            var item = items.get(0);
                            if (item.getTransDt() != null && !item.getTransDt().isBlank()) {
                                billStatusHistoryService.saveGovTransfer(billId, item);
                                log.info("✅ 정부 이송 반영 완료 - billId: {}, transDt: {}", billId, item.getTransDt());
                                updated.incrementAndGet();
                            } else {
                                log.debug("ℹ️ 정부 이송일자 없음 - billId: {}", billId);
                            }
                        } else {
                            log.debug("ℹ️ 정부 이송 항목 없음 - billId: {}", billId);
                        }
                    });
                    triedCount++;
                } catch (Exception e) {
                    log.warn("❌ 정부 이송 반영 실패 - billId: {}", billId, e);
                }

                Thread.sleep(SLEEP_MS);
                if (triedCount >= MAX_COUNT) break;
            }

            if (!billPage.hasNext()) break;
            page++;
        }

        log.info("🎯 정부 이송 시도: {}건, 성공 반영: {}건", triedCount, updated.get());
        return RepeatStatus.FINISHED;
    }
}
