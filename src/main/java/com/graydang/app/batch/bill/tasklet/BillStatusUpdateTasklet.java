package com.graydang.app.batch.bill.tasklet;

import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.BillStatusHistory;
import com.graydang.app.domain.bill.repository.BillRepository;
import com.graydang.app.domain.bill.repository.BillStatusHistoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillStatusUpdateTasklet implements Tasklet {

    private final BillRepository billRepository;
    private final BillStatusHistoryRepository billStatusHistoryRepository;

    @PersistenceContext
    private EntityManager em;

    private static final int BATCH_SIZE = 500;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<Bill> bills = billRepository.findAll();
        int count = 0;

        for (int i = 0; i < 500; i++) {
            Bill bill = bills.get(i);

            Optional<BillStatusHistory> latest = billStatusHistoryRepository.findTopByBillOrderByStepOrderDesc(bill);
            if (latest.isPresent()) {
                bill.updateBillStatus(latest.get().getStatus()); // 도메인 메서드
                count++;
            }

            if (i % BATCH_SIZE == 0) {
                em.flush();
                em.clear();
            }
        }

        em.flush();
        em.clear();

        log.info("업데이트된 bill status 개수: {}", count);
        return RepeatStatus.FINISHED;
    }
}
