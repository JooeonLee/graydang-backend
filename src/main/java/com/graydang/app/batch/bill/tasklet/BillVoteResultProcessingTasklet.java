package com.graydang.app.batch.bill.tasklet;

import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillVoteResultResponseDto;
import com.graydang.app.domain.bill.repository.BillRepository;
import com.graydang.app.domain.bill.service.BillVoteResultService;
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
@Component
@RequiredArgsConstructor
public class BillVoteResultProcessingTasklet implements Tasklet {

    private final BillApiClient billApiClient;
    private final BillRepository billRepository;
    private final BillVoteResultService billVoteResultService;

    private static final int PAGE_SIZE = 50;
    private static final int MAX_COUNT = 500;
    private static final int SLEEP_MS = 100;
    private static final int AGE = 22; // 제안대수

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        AtomicInteger saved = new AtomicInteger();
        int triedCount = 0;
        int page = 0;

        while (triedCount < MAX_COUNT) {
            Page<String> billPage = billRepository.findLatestBillIds(PageRequest.of(page, PAGE_SIZE));
            List<String> billIds = billPage.getContent();

            if (billIds.isEmpty()) break;

            for (String billId : billIds) {
                try {
                    billApiClient.getBillVoteResultInfo(billId).ifPresent(dto -> {
                        BillVoteResultResponseDto.Item item = dto.getItem();
                        if (item != null && item.getProcDate() != null && !item.getProcDate().isBlank()) {
                            billVoteResultService.saveVoteResult(billId, item);
                            log.info("✅ 표결 결과 저장 완료 - billId: {}, 찬성: {}, 반대: {}",
                                    billId, item.getYesCount(), item.getNoCount());
                            saved.incrementAndGet();
                        } else {
                            log.debug("ℹ️ 표결 결과 item 없음 또는 procDate 누락 - billId: {}", billId);
                        }
                    });
                    triedCount++;
                } catch (Exception e) {
                    log.warn("❌ 표결 결과 저장 실패 - billId: {}", billId, e);
                }

                Thread.sleep(SLEEP_MS);
                if (triedCount >= MAX_COUNT) break;
            }

            if (!billPage.hasNext()) break;
            page++;
        }

        log.info("🎯 표결 결과 시도: {}건, 성공 저장: {}건", triedCount, saved.get());
        return RepeatStatus.FINISHED;
    }
}
