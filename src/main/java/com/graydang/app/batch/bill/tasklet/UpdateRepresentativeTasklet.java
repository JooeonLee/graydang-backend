package com.graydang.app.batch.bill.tasklet;

import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.repository.BillRepository;
import com.graydang.app.domain.bill.service.BillParser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UpdateRepresentativeTasklet implements Tasklet {

    private final BillRepository billRepository;

    @PersistenceContext
    private EntityManager em;

    private static final int BATCH_SIZE = 500;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<Bill> bills = billRepository.findAll(); // 모든 bill 대상으로 (공백도 걸러야 하므로)
        int count = 0;

        for (int i = 0; i < bills.size(); i++) {
            Bill bill = bills.get(i);

            // null 또는 공백 문자열만 처리
            if (bill.getRepresentativeName() != null && !bill.getRepresentativeName().isBlank()) {
                continue;
            }

            String parsed = BillParser.extractProposer(bill.getTitle());
            bill.registerRepresentative(parsed);

            if (parsed != null && !parsed.isBlank()) {
                count++;
            }

            if (i % BATCH_SIZE == 0) {
                em.flush();
                em.clear();
            }
        }

        em.flush(); // 남은 것 처리
        em.clear();

        System.out.println("대표발의자 업데이트 수: " + count);
        return RepeatStatus.FINISHED;
    }
}
