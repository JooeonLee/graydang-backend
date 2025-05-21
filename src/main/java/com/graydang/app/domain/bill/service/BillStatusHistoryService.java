package com.graydang.app.domain.bill.service;

import com.graydang.app.batch.bill.dto.BillCommissionResponseDto;
import com.graydang.app.batch.bill.dto.BillDeliverateInfoResponseDto;
import com.graydang.app.batch.bill.dto.BillPromulgationInfoResponseDto;
import com.graydang.app.batch.bill.dto.BillTransferredInfoResponseDto;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.BillStatusHistory;
import com.graydang.app.domain.bill.repository.BillRepository;
import com.graydang.app.domain.bill.repository.BillStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BillStatusHistoryService {

    private final BillRepository billRepository;
    private final BillStatusHistoryRepository billStatusHistoryRepository;

    private static final String STEP_NAME = "위원회 회부";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public void saveCommitteeExamination(String billId, BillCommissionResponseDto.JurisdictionExaminationItem item) {
        if (item == null) {
            log.warn("위원회 회부 item이 null 입니다. - billId: {}", billId);
            return;
        }

        Bill bill = billRepository.findByBillId(billId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 billId를 찾을 수 없습니다: " + billId));

        LocalDate stepDate = parseDate(item.getProcDt());
        String stepResult = item.getProcResultCd();

        if (stepDate == null || stepResult == null || stepResult.isBlank()) {
            log.info("심사 처리 정보가 없어 저장 생략 - billId: {}", billId);
            return;
        }

        Optional<BillStatusHistory> existingOpt = billStatusHistoryRepository.findByBillAndStepName(bill, STEP_NAME);

        if(existingOpt.isPresent()) {
            BillStatusHistory existing = existingOpt.get();
            existing.update(stepDate, stepResult, STATUS_ACTIVE);
            log.info("위원회 회부 이력 업데이트 완료 - billId: {}, result: {}", billId, stepResult);
        } else {
            BillStatusHistory history = BillStatusHistory.builder()
                    .bill(bill)
                    .stepOrder(1)
                    .stepName(STEP_NAME)
                    .stepDate(parseDate(item.getProcDt()))
                    .stepResult(item.getProcResultCd())
                    .status(STATUS_ACTIVE)
                    .build();

            billStatusHistoryRepository.save(history);
            log.info("위원회 회부 이력 저장 완료 - billId: {}, date: {}, result: {}", billId, item.getProcDt(), item.getProcResultCd());
        }
    }

    @Transactional
    public void savePlenaryExamination(String billId, BillDeliverateInfoResponseDto.PlenarySessionExaminationItem item) {
        if (item == null) return;

        LocalDate stepDate = parseDate(item.getProcDt());
        String stepResult = item.getProcResultCd();

        if (stepDate == null || stepResult == null || stepResult.isBlank()) {
            log.info("본회의 심의 처리 정보 누락 - 저장 생략 - billId: {}", billId);
            return;
        }

        Bill bill = billRepository.findByBillId(billId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 billId를 찾을 수 없습니다: " + billId));

        final String STEP_NAME = "본회의 심의";
        final String STATUS_ACTIVE = "ACTIVE";

        billStatusHistoryRepository.findByBillAndStepName(bill, STEP_NAME)
                .ifPresentOrElse(
                        existing -> {
                            existing.update(stepDate, stepResult, STATUS_ACTIVE);
                            log.info("본회의 심의 이력 업데이트 - billId: {}", billId);
                        },
                        () -> {
                            BillStatusHistory history = BillStatusHistory.builder()
                                    .bill(bill)
                                    .stepOrder(2)
                                    .stepName(STEP_NAME)
                                    .stepDate(stepDate)
                                    .stepResult(stepResult)
                                    .status(STATUS_ACTIVE)
                                    .build();
                            billStatusHistoryRepository.save(history);
                            log.info("본회의 심의 이력 저장 완료 - billId: {}", billId);
                        });
    }

    @Transactional
    public void saveGovTransfer(String billId, BillTransferredInfoResponseDto.TransferredItem item) {
        if (item.getTransDt() == null || item.getTransDt().isBlank()) {
            log.debug("정부 이송일자 없음 - billId: {}", billId);
            return;
        }

        Bill bill = billRepository.findByBillId(billId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 billId를 찾을 수 없습니다: " + billId));

        String stepName = "정부 이송";
        LocalDate stepDate = parseDate(item.getTransDt());

        billStatusHistoryRepository.findByBillAndStepName(bill, stepName)
                .ifPresentOrElse(
                        existing -> {
                            existing.update(stepDate, null, "ACTIVE");
                            log.info("정부 이송 이력 업데이트 - billId: {}, date: {}", billId, stepDate);
                        },
                        () -> {
                            BillStatusHistory history = BillStatusHistory.builder()
                                    .bill(bill)
                                    .stepOrder(3)
                                    .stepName(stepName)
                                    .stepDate(stepDate)
                                    .stepResult(stepName)
                                    .status("ACTIVE")
                                    .build();
                            billStatusHistoryRepository.save(history);
                            log.info("정부 이송 이력 저장 - billId: {}, date: {}", billId, stepDate);
                        }
                );
    }

    @Transactional
    public void savePromulgation(String billId, BillPromulgationInfoResponseDto.PromulgationItem item) {
        if (item.getAnounceDt() == null || item.getAnounceDt().isBlank()) {
            log.debug("ℹ️ 공포일자 없음 - billId: {}", billId);
            return;
        }

        Bill bill = billRepository.findByBillId(billId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 billId를 찾을 수 없습니다: " + billId));

        String stepName = "공포";
        LocalDate stepDate = parseDate(item.getAnounceDt());
        String stepResult = item.getLawTitle(); // 또는 별도로 저장하지 않을 수도 있음

        Optional<BillStatusHistory> existingOpt = billStatusHistoryRepository.findByBillAndStepName(bill, stepName);

        if (existingOpt.isPresent()) {
            existingOpt.get().update(stepDate, stepResult, STATUS_ACTIVE);
            log.info("🔁 공포 이력 업데이트 - billId: {}, 공포일자: {}", billId, item.getAnounceDt());
        } else {
            BillStatusHistory history = BillStatusHistory.builder()
                    .bill(bill)
                    .stepOrder(4)
                    .stepName(stepName)
                    .stepDate(stepDate)
                    .stepResult(stepResult)
                    .status(STATUS_ACTIVE)
                    .build();
            billStatusHistoryRepository.save(history);
            log.info("✅ 공포 이력 저장 완료 - billId: {}, 공포일자: {}", billId, item.getAnounceDt());
        }
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        return LocalDate.parse(date, DATE_FORMATTER);
    }
}
