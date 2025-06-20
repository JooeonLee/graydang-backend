package com.graydang.app.batch.bill.tasklet;

import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillCommissionResponseDto;
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
public class BillCommitteeProcessingTasklet implements Tasklet {

    private final BillApiClient billApiClient;
    private final BillService billService;
    private final BillRepository billRepository;
    private final BillStatusHistoryService billStatusHistoryService;

    private static final int PAGE_SIZE = 100;
    private static final int MAX_COUNT = 10000;
    private static final int SLEEP_MS = 50;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        AtomicInteger updated = new AtomicInteger();
        int triedCount = 0;
        int page = 0;

        while (updated.get() < MAX_COUNT) {
            Page<String> billPage = billRepository.findLatestBillIds(PageRequest.of(page, PAGE_SIZE));
            List<String> billIds = billPage.getContent();

            if (billIds.isEmpty()) break;

            for (String billId : billIds) {
                try {
                    billApiClient.getBillCommissionInfo(billId).ifPresent(dto -> {
                        List<BillCommissionResponseDto.JurisdictionExaminationItem> items = dto.getBody().getJurisdictionExamination();
                        if (items != null && !items.isEmpty()) {
                            BillCommissionResponseDto.JurisdictionExaminationItem item = items.get(0);

                            // 1. Bill 업데이트
                            billService.updateCommitteeName(billId, item.getCommitteeName());

                            // 2. BillStatusHistory 업데이트
                            billStatusHistoryService.saveCommitteeExamination(billId, item);

                            log.info("✅ 반영 완료 - billId: {}, committeeName: {}, result: {}",
                                    billId, item.getCommitteeName(), item.getProcResultCd());
                            updated.getAndIncrement();
                        }
                    });
                    triedCount++;
                } catch (Exception e) {
                    log.warn("❌ 반영 실패 - billId: {}", billId, e);
                }

                Thread.sleep(SLEEP_MS);
                if (triedCount >= MAX_COUNT) break;
            }

            if (!billPage.hasNext()) break;
            page++;
        }

        log.info("🎯 총 {}건의 소관위원회 정보가 갱신되었습니다.", updated);
        return RepeatStatus.FINISHED;
    }
}
