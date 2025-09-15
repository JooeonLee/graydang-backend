package com.graydang.app.batch.bill.jobs.committee.writer;

import com.graydang.app.batch.bill.jobs.committee.dto.BillUpdateDto;
import com.graydang.app.domain.bill.service.BillService;
import com.graydang.app.domain.bill.service.BillStatusHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillCommitteeItemWriter implements ItemWriter<BillUpdateDto> {

    private final BillService billService;
    private final BillStatusHistoryService billStatusHistoryService;

    @Override
    public void write(Chunk<? extends BillUpdateDto> chunk) throws Exception {
        log.info("Writing a chunk of size: {}", chunk.size());
        for (BillUpdateDto dto : chunk.getItems()) {
            // 이 블록 전체가 하나의 트랜잭션으로 묶여 처리됩니다.
            billService.updateCommitteeName(dto.billId(), dto.item().getCommitteeName());
            billStatusHistoryService.saveCommitteeExamination(dto.billId(), dto.item());
        }
    }
}
