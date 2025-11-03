package com.graydang.app.domain.admin.service;

import com.graydang.app.domain.admin.model.dto.BillUpdateRequestDto;
import com.graydang.app.domain.bill.exception.BillException;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.repository.BillRepository;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBillService {

    private final BillRepository billRepository;

    @Transactional
    public void updateBill(Long billId, BillUpdateRequestDto requestDto, Long adminId) {
        log.info("법안 수정 요청 - billId: {}, adminId: {}", billId, adminId);
        
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillException(BaseResponseStatus.NONE_BILL));

        log.info("법안 수정 전 - billId: {}, title: {}, status: {}", 
                bill.getId(), bill.getTitle(), bill.getBillStatus());

        // 법안 업데이트 - Envers가 자동으로 이력 추적
        bill.updateByAdmin(
                requestDto.getTitle(),
                requestDto.getSummary(),
                requestDto.getAiTitle(),
                requestDto.getAiSummary(),
                requestDto.getCommitteeName(),
                requestDto.getBillStatus()
        );

        log.info("법안 수정 완료 - billId: {}, 수정자: {}", billId, adminId);
    }
}