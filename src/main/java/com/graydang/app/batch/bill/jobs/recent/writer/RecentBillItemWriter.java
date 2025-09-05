package com.graydang.app.batch.bill.jobs.recent.writer;

import com.graydang.app.batch.bill.dto.BillSaveRequestDto;
import com.graydang.app.domain.bill.repository.BillRepository;
import com.graydang.app.domain.bill.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class RecentBillItemWriter implements ItemWriter<BillSaveRequestDto> {

    private final BillService billService;

    @Override
    public void write(Chunk<? extends BillSaveRequestDto> chunk) {
        if (chunk == null || chunk.isEmpty())
            return;

        billService.saveAll(chunk.getItems());
        log.info("Saved Recent Bills: {}", chunk.getItems().size());
    }
}
