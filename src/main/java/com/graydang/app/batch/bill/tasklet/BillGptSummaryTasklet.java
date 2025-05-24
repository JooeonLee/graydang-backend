package com.graydang.app.batch.bill.tasklet;

import com.graydang.app.batch.bill.client.GptApiClient;
import com.graydang.app.batch.bill.dto.BillGptSummaryDto;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.repository.BillRepository;
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
public class BillGptSummaryTasklet implements Tasklet {

    private final BillRepository billRepository;
    private final GptApiClient gptApiClient;

    private static final int PAGE_SIZE = 5;
    private static final int MAX_COUNT = 50;
    private static final int SLEEP_MS = 1500;

//    @Override
//    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//        AtomicInteger updated = new AtomicInteger();
//        int triedCount = 0;
//        int page = 0;
//
//        while (triedCount < MAX_COUNT) {
//            Page<Bill> billPage = billRepository.findByAiProcessedFalse(PageRequest.of(page, PAGE_SIZE));
//            List<Bill> bills = billPage.getContent();
//
//            if (bills.isEmpty()) break;
//
//            for (Bill bill : bills) {
//                try {
//                    BillGptSummaryDto result = gptApiClient.generateTitleAndSummary(
//                            bill.getTitle(),
//                            bill.getSummary() // 또는 bill.getSummary() 등 실제 사용 컬럼
//                    );
//
//                    bill.updateAiSummary(result.getTitle(), result.getSummary());
//                    billRepository.save(bill);
//
//                    log.info("✅ GPT 요약 완료 - billId: {}, 제목: {}", bill.getId(), result.getTitle());
//                    updated.getAndIncrement();
//                } catch (Exception e) {
//                    log.warn("❌ GPT 처리 실패 - billId: {}", bill.getId(), e);
//                }
//
//                Thread.sleep(SLEEP_MS);
//                triedCount++;
//                if (triedCount >= MAX_COUNT) break;
//            }
//
//            if (!billPage.hasNext()) break;
//            page++;
//        }
//
//        log.info("🎯 GPT 요약/제목 반영 완료: {}건", updated);
//        return RepeatStatus.FINISHED;
//    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        AtomicInteger updated = new AtomicInteger();
        int triedCount = 0;

        while (triedCount < MAX_COUNT) {
            // 항상 첫 페이지만 조회
            Page<Bill> billPage = billRepository.findByAiProcessedFalse(PageRequest.of(0, PAGE_SIZE));
            List<Bill> bills = billPage.getContent();

            if (bills.isEmpty()) break;

            for (Bill bill : bills) {
                try {
                    BillGptSummaryDto result = gptApiClient.generateTitleAndSummary(
                            bill.getTitle(),
                            bill.getSummary()
                    );

                    bill.updateAiSummary(result.getTitle(), result.getSummary());
                    billRepository.save(bill);

                    log.info("✅ GPT 요약 완료 - billId: {}, 제목: {}", bill.getId(), result.getTitle());
                    updated.incrementAndGet();
                } catch (Exception e) {
                    log.warn("❌ GPT 처리 실패 - billId: {}", bill.getId(), e);
                }

                Thread.sleep(SLEEP_MS);
                triedCount++;
                if (triedCount >= MAX_COUNT) break;
            }
        }

        log.info("🎯 GPT 요약/제목 반영 완료: {}건", updated);
        return RepeatStatus.FINISHED;
    }

//    @Override
//    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//        Optional<Bill> optionalBill = billRepository.findFirstByAiProcessedFalseOrderByIdAsc();
//
//        if (optionalBill.isEmpty()) {
//            log.info("📭 처리할 미처리 법안이 없습니다.");
//            return RepeatStatus.FINISHED;
//        }
//
//        Bill bill = optionalBill.get();
//
//        try {
//            BillGptSummaryDto result = gptApiClient.generateTitleAndSummary(
//                    bill.getTitle(),
//                    bill.getSummary() // 또는 summary 등
//            );
//
//            bill.updateAiSummary(result.getTitle(), result.getSummary());
//            billRepository.save(bill);
//
//            log.info("✅ 1건 GPT 요약 완료 - billId: {}, 제목: {}", bill.getId(), result.getTitle());
//        } catch (Exception e) {
//            log.warn("❌ GPT 처리 실패 - billId: {}", bill.getId(), e);
//        }
//
//        return RepeatStatus.FINISHED;
//    }
}
