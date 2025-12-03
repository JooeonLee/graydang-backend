package com.graydang.app.batch.bill.jobs.update.plenary.writer;

import com.graydang.app.batch.bill.dto.BillDeliverateInfoResponseDto;
import com.graydang.app.batch.bill.jobs.update.plenary.dto.BillStatusHistoryPlenaryUpdateDto;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.repository.BillRepository;
import com.graydang.app.domain.bill.service.BillService;
import com.graydang.app.domain.bill.service.BillStatusHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillStatusHistoryPlenaryProcessingItemWriter implements ItemWriter<BillStatusHistoryPlenaryUpdateDto> {

    private final BillRepository billRepository;
    private final BillService billService;
    private final BillStatusHistoryService billStatusHistoryService;

    /**
     * Processor가 전달한 DTO 청크를 받아 DB에 일괄 저장합니다.
     *
     * @param chunk Processor에서 전달된 BillStatusHistoryPlenaryProcessingUpdateDto의 묶음
     */
    @Override
    public void write(Chunk<? extends BillStatusHistoryPlenaryUpdateDto> chunk) throws Exception {

        List<Long> billIds = chunk.getItems().stream()
                .map(BillStatusHistoryPlenaryUpdateDto::getId)
                .toList();

        List<Bill> bills = billRepository.findAllById(billIds);

        Map<Long, Bill> billMap = bills.stream()
                .collect(Collectors.toMap(Bill::getId, Function.identity()));

        for (BillStatusHistoryPlenaryUpdateDto dto : chunk.getItems()) {
            BillDeliverateInfoResponseDto.PlenarySessionExaminationItem item = dto.getPlenarySessionItem();

            String procDt = item.getProcDt();
            if(procDt == null || procDt.isBlank()) {
                log.info("⚠️ [WRITE_SKIP] '심사일'(procDt)이 없어 저장을 생략합니다. billId: {}", dto.getBillId());
                continue; // 이 아이템은 스킵하고 다음 DTO로 넘어감
            }

            // Map에서 조회 (DB 접근 아님)
            Bill bill = billMap.get(dto.getId());

            // 본회의 심의 결과 업데이트 서비스 호출
            billStatusHistoryService.saveNewPlenaryReferral(bill, item);
        }
    }
}
