package com.graydang.app.domain.bill.service;

import com.graydang.app.batch.bill.dto.BillCommissionResponseDto;
import com.graydang.app.batch.bill.dto.BillDeliverateInfoResponseDto;
import com.graydang.app.batch.bill.dto.BillPromulgationInfoResponseDto;
import com.graydang.app.batch.bill.dto.BillTransferredInfoResponseDto;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.BillStatusHistory;
import com.graydang.app.domain.bill.repository.BillRepository;
import com.graydang.app.domain.bill.repository.BillStatusHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
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
        Bill bill = billRepository.findByBillId(billId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 billId를 찾을 수 없습니다: " + billId));

        saveCommitteeExaminationLogic(bill, item);
    }

    @Transactional
    public void saveCommitteeExamination(Bill bill, BillCommissionResponseDto.JurisdictionExaminationItem item) {
        saveCommitteeExaminationLogic(bill, item);
    }

    private void saveCommitteeExaminationLogic(Bill bill, BillCommissionResponseDto.JurisdictionExaminationItem item) {
        if (item == null) {
            log.warn("위원회 회부 item이 null 입니다. - billId: {}", bill.getBillId());
            return;
        }

        LocalDate stepDate = parseDate(item.getProcDt());
        String stepResult = item.getProcResultCd();

        if (stepDate == null || stepResult == null || stepResult.isBlank()) {
            log.info("심사 처리 정보가 없어 저장 생략 - billId: {}", bill.getBillId());
            return;
        }

        Optional<BillStatusHistory> existingOpt = billStatusHistoryRepository.findByBillAndStepName(bill, STEP_NAME);

        if(existingOpt.isPresent()) {
            BillStatusHistory existing = existingOpt.get();
            existing.update(stepDate, stepResult, STATUS_ACTIVE);
            log.info("위원회 회부 이력 업데이트 완료 - billId: {}, result: {}", bill.getBillId(), stepResult);
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
            log.info("위원회 회부 이력 저장 완료 - billId: {}, date: {}, result: {}", bill.getBillId(), item.getProcDt(), item.getProcResultCd());
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

    /**
     * [Job 1: 발의 -> 심사 중] 배치를 위한 전용 메서드.
     * '회부일'(submitDt)이 확인되면 '소관위 회부' 이력을 신규 생성(INSERT)합니다.
     *
     * @param bill  Writer가 조회한 Bill 엔티티
     * @param item  API 응답 (소관위 심사 정보)
     */
    @Transactional
    public void saveNewCommitteeReferral(Bill bill, BillCommissionResponseDto.JurisdictionExaminationItem item) {
        if (item == null) {
            log.warn("위원회 회부 item이 null 입니다. - billId: {}", bill.getBillId());
            return;
        }

        // (1) '회부일'(submitDt)을 기준 날짜로 사용합니다.
        LocalDate stepDate = parseDate(item.getSubmitDt());

        // (2) '회부일'이 없으면 '소관위 회부'가 아니므로 저장 생략
        if (stepDate == null) {
            log.info("핵심 정보인 '회부일'(submitDt)이 없어 저장 생략 - billId: {}", bill.getBillId());
            return;
        }

        // (3) api 응답값 확인
        String apiStepResult = item.getProcResultCd();

        // (4) api 결과가 없으면 "심사 중"을 기본값으로 설정
        String finalStepResult = (apiStepResult == null || apiStepResult.isBlank()) ? "심사 중" : apiStepResult;

        // (3) [중요] 이 메서드는 Job 1(신규) 전용이므로, existingOpt 검사를 *하지 않습니다.*
        // Reader가 (WHERE NOT EXISTS)로 신규 의안만 가져왔다고 신뢰합니다.

        BillStatusHistory history = BillStatusHistory.builder()
                .bill(bill)
                .stepOrder(1)
                .stepName(STEP_NAME)     // "소관위 회부"
                .stepDate(stepDate)      // ★ 회부일
                .stepResult(finalStepResult)
                .status(STATUS_ACTIVE)
                .build();

        billStatusHistoryRepository.save(history);
        log.info("위원회 '심사 중' 이력 신규 저장 완료 - billId: {}, date: {}", bill.getBillId(), item.getSubmitDt());
    }

    /**
     * [Job 2: 심사 중 -> 심사 완료] 배치를 위한 전용 메서드.
     * API 응답에 '처리 결과'(procResultCd)가 존재하면, 기존 이력을 업데이트합니다.
     */
    @Transactional
    public void updateCommitteeExaminationResult(Bill bill, BillCommissionResponseDto.JurisdictionExaminationItem item) {
        // 1. 처리 결과(procResultCd) 확인
        String procResult = item.getProcResultCd();

        // 2. 처리 결과가 없으면 아직 '심사 중'인 것이므로 변경사항 없음 -> 종료
        if (procResult == null || procResult.isBlank()) {
            // (로그 레벨은 info)
            log.debug("아직 심사 중입니다. 업데이트를 건너뜁니다. - billId: {}", bill.getBillId());
            return;
        }

        // 3. 기존 이력 조회 ('소관위 회부' 단계)
        // Reader가 '심사 중'인 데이터를 가져왔으므로 데이터는 반드시 존재해야 함
        BillStatusHistory history = billStatusHistoryRepository.findByBillAndStepName(bill, "위원회 회부")
                .orElseThrow(() -> new EntityNotFoundException("이력 데이터 불일치: " + bill.getBillId()));

        // 4. 상태 업데이트 (처리일, 처리결과)
        LocalDate procDate = parseDate(item.getProcDt()); // 처리일 (없으면 회부일 유지 등의 로직 필요 시 추가)

        // 업데이트 실행 (Dirty Checking)
        history.update(procDate, procResult, "ACTIVE");

        // (선택) Bill 테이블의 bill_status도 업데이트가 필요하다면 수행
        bill.updateBillStatus(procResult);

        log.info("✅ 소관위 심사 완료 업데이트 - billId: {}, 결과: {}", bill.getBillId(), procResult);
    }

    /**
     * [Job 1: 위원회 회부 -> 본회의 심의] 배치를 위한 전용 메서드.
     * '처리일'(procDt)이 확인되면 '본회의 심의' 이력을 신규 생성(INSERT)합니다.
     *
     * @param bill  Writer가 조회한 Bill 엔티티
     * @param item  API 응답 (본회의 심의 정보)
     */
    @Transactional
    public void saveNewPlenaryReferral(Bill bill, BillDeliverateInfoResponseDto.PlenarySessionExaminationItem item) {
        if (item == null) {
            log.warn("본회의 심의 item이 null 입니다. - billId: {}", bill.getBillId());
            return;
        }

        // (1) '처리일'(procDt)을 기준 날짜로 사용합니다.
        LocalDate stepDate = parseDate(item.getProcDt());

        // (2) '처리일'이 없으면 '본회의 심의'가 아니므로 저장 생략
        if (stepDate == null) {
            log.info("핵심 정보인 '처리일'(procDt)이 없어 저장 생략 - billId: {}", bill.getBillId());
            return;
        }

        // (3) api 응답값 확인
        String apiProcResult = item.getProcResultCd();

        // (4) api 결과가 없으면 "심사 중"을 기본값으로 설정
        String finalStepResult = (apiProcResult == null || apiProcResult.isBlank()) ? "심사 중" : apiProcResult;

        // (3) [중요] 이 메서드는 Job 1(신규) 전용이므로, existingOpt 검사를 *하지 않습니다.*
        // Reader가 (WHERE NOT EXISTS)로 신규 의안만 가져왔다고 신뢰합니다.

        BillStatusHistory history = BillStatusHistory.builder()
                .bill(bill)
                .stepOrder(2)
                .stepName("본회의 심의")     // "본회의 심의"
                .stepDate(stepDate)      // ★ 회부일
                .stepResult(finalStepResult)
                .status(STATUS_ACTIVE)
                .build();

        billStatusHistoryRepository.save(history);

        bill.updateBillStatus(finalStepResult);

        log.info("본회의 심의 이력 신규 저장 완료 - billId: {}, date: {}", bill.getBillId(), item.getProcDt());
    }

    /**
     * [Job 1: 본회의 심의 -> 정부 이송] 배치를 위한 전용 메서드.
     * '이송일'(transDt)이 확인되면 '정부 이송' 이력을 신규 생성(INSERT)합니다.
     *
     * @param bill  Writer가 조회한 Bill 엔티티
     * @param item  API 응답 (정부 이송 정보)
     */
    @Transactional
    public void
    saveNewGovTransferReferral(Bill bill, BillTransferredInfoResponseDto.TransferredItem item) {
        if (item == null) {
            log.warn("본회의 심의 item이 null 입니다. - billId: {}", bill.getBillId());
            return;
        }

        // (1) '이송일'(transDt)을 기준 날짜로 사용합니다.
        LocalDate stepDate = parseDate(item.getTransDt());

        // (2) '이송일'이 없으면 '정부 이송'이 아니므로 저장 생략
        if (stepDate == null) {
            log.info("핵심 정보인 '이송일'(transDt)이 없어 저장 생략 - billId: {}", bill.getBillId());
            return;
        }

        // (3) [중요] 이 메서드는 Job 1(신규) 전용이므로, existingOpt 검사를 *하지 않습니다.*
        // Reader가 (WHERE NOT EXISTS)로 신규 의안만 가져왔다고 신뢰합니다.

        String transferResult = "정부 이송";

        BillStatusHistory history = BillStatusHistory.builder()
                .bill(bill)
                .stepOrder(3)
                .stepName("정부 이송")     // "정부 이송"
                .stepDate(stepDate)      // ★ 회부일
                .stepResult(transferResult)
                .status(STATUS_ACTIVE)
                .build();

        billStatusHistoryRepository.save(history);

        bill.updateBillStatus(transferResult);

        log.info("정부 이송 이력 신규 저장 완료 - billId: {}, date: {}", bill.getBillId(), item.getTransDt());
    }

    /**
     * [Job 1: 본회의 심의 -> 정부 이송] 배치를 위한 전용 메서드.
     * '이송일'(transDt)이 확인되면 '정부 이송' 이력을 신규 생성(INSERT)합니다.
     *
     * @param bill  Writer가 조회한 Bill 엔티티
     * @param item  API 응답 (정부 이송 정보)
     */
    @Transactional
    public void
    saveNewPromulgationReferral(Bill bill, BillPromulgationInfoResponseDto.PromulgationItem item) {
        if (item == null) {
            log.warn("공포 item이 null 입니다. - billId: {}", bill.getBillId());
            return;
        }

        // (1) '공포일'(anounceDt)을 기준 날짜로 사용합니다.
        LocalDate stepDate = parseDate(item.getAnounceDt());
        String lawTitle = item.getLawTitle();

        // (2) '이송일'이 없으면 '정부 이송'이 아니므로 저장 생략
        if (stepDate == null || lawTitle == null) {
            log.info("핵심 정보인 '공포일'(anounceDt) 혹은 의안 제목이 없어 저장 생략 - billId: {}", bill.getBillId());
            return;
        }

        // (3) [중요] 이 메서드는 Job 1(신규) 전용이므로, existingOpt 검사를 *하지 않습니다.*
        // Reader가 (WHERE NOT EXISTS)로 신규 의안만 가져왔다고 신뢰합니다.

        BillStatusHistory history = BillStatusHistory.builder()
                .bill(bill)
                .stepOrder(4)
                .stepName("공포")     // "공포"
                .stepDate(stepDate)      // ★ 회부일
                .stepResult(lawTitle)
                .status(STATUS_ACTIVE)
                .build();

        billStatusHistoryRepository.save(history);

        bill.updateBillStatus(lawTitle);

        log.info("공포 이력 신규 저장 완료 - billId: {}, date: {}", bill.getBillId(), item.getAnounceDt());
    }
}
