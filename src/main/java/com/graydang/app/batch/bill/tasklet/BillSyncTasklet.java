package com.graydang.app.batch.bill.tasklet;

import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillInfoResponseDto;
import com.graydang.app.domain.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class BillSyncTasklet implements Tasklet {

    private final BillApiClient billApiClient;
    private final BillService billService;



    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        log.info("📦 [의안 수집 배치] 시작");

        int pageNo = 1;
        int numOfRows = 100;
        int totalCount = 0;

        while (true) {
            List<BillInfoResponseDto.ItemDto> items = billApiClient.getBillInfoList(numOfRows, pageNo);
            if (items == null || items.isEmpty()) {
                log.info("✔️ 수집 종료: 더 이상 수집할 데이터 없음 (pageNo={})", pageNo);
                break;
            }

            for (BillInfoResponseDto.ItemDto item : items) {
                billService.saveOrUpdate(item);
            }

            totalCount += items.size();
            log.info("📄 pageNo={} 처리 완료 (누적: {}건)", pageNo, totalCount);
            pageNo++;
        }

        log.info("✅ [의안 수집 배치] 완료 - 총 {}건 수집됨", totalCount);
        return RepeatStatus.FINISHED;
    }
}
