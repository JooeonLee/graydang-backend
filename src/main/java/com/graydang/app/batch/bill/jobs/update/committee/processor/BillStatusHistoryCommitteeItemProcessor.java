package com.graydang.app.batch.bill.jobs.update.committee.processor;

import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillCommissionResponseDto;
import com.graydang.app.batch.bill.jobs.update.committee.dto.BillStatusHistoryCommitteeUpdateDto;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.BillStatusHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component // (1) Spring 빈으로 등록
@RequiredArgsConstructor
public class BillStatusHistoryCommitteeItemProcessor implements ItemProcessor<Bill, BillStatusHistoryCommitteeUpdateDto> {

    private final BillApiClient billApiClient;

    // (2) Tasklet에 있던 API 스로틀링(속도 제어) 로직
    private static final int SLEEP_MS = 50;

    /**
     * Reader가 읽은 'Bill' 객체를 받아 API 호출 및 데이터 가공을 수행합니다.
     *
     * @param bill Reader가 읽어온 'bill' 테이블 엔티티
     * @return '소관위 회부' 정보가 확인되면 Writer로 넘길 DTO, 없으면 null (필터링)
     */
    @Override
    public BillStatusHistoryCommitteeUpdateDto process(Bill bill) throws Exception {
        try {
            // (3) Tasklet의 Thread.sleep() 로직
            // 멀티스레드 환경에서도 각 스레드(Processor)가 API 호출 속도를 조절합니다.
            Thread.sleep(SLEEP_MS);

            // (4) Tasklet의 API 호출 로직 (bill.getBillId() 사용)
            Optional<BillCommissionResponseDto> responseOpt = billApiClient.getBillCommissionInfo(bill.getBillId());

            if (responseOpt.isPresent()) {
                // (5) Tasklet의 파싱 및 유효성 검증 로직
                List<BillCommissionResponseDto.JurisdictionExaminationItem> items =
                        responseOpt.get().getBody().getJurisdictionExamination();

                if (items != null && !items.isEmpty()) {
                    BillCommissionResponseDto.JurisdictionExaminationItem item = items.get(0);

                    // (6) [핵심] 유효한 데이터를 Writer로 전달
                    // Tasklet과 달리 DB 저장을 하지 않고, DTO를 생성하여 반환합니다.
                    // Writer가 Bill PK (bill.getId())와 API 결과(item)를 모두 쓸 수 있게 전달합니다.
                    return new BillStatusHistoryCommitteeUpdateDto(bill.getId(), bill.getBillId(), item);
                }
            }

            // (7) API를 호출했으나 '소관위 회부' 정보가 없는 경우 (아직 발의 상태)
            // null을 반환하여 Writer로 전달되지 않도록 필터링합니다.
            return null;

        } catch (InterruptedException ie) {
            // 멀티스레드 중단 시
            log.warn("Processor Interrupted - billId: {}", bill.getBillId());
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
            return null;
        } catch (Exception e) {
            // (8) Tasklet의 예외 처리 로직 (API 호출 오류 등)
            log.warn("❌ API 처리 실패 - billId: {}. 이 항목을 Skip합니다. Error: {}",
                    bill.getBillId(), e.getMessage());
            // 실패한 항목은 null을 반환하여 이번 배치에서는 제외 (다음 배치에서 재시도)
            return null;
        }
    }
}
