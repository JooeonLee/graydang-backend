package com.graydang.app.domain.service;

import com.graydang.app.batch.bill.dto.BillInfoResponseDto;
import com.graydang.app.domain.bill.Bill;
import com.graydang.app.domain.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BillService {

    private final BillRepository billRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

    @Transactional
    public void saveOrUpdate(BillInfoResponseDto.ItemDto dto) {
        Optional<Bill> existing = billRepository.findByBillId(dto.getBillId());

        if (existing.isPresent()) {
            update(existing.get(), dto);
        } else {
            Bill bill = toEntity(dto);
            billRepository.save(bill);
        }
    }

    private Bill toEntity(BillInfoResponseDto.ItemDto dto) {
        return Bill.builder()
                .billId(dto.getBillId())
                .title(dto.getBillName())
                .proposeDate(parseDate(dto.getProposeDt()))
                .committeeName(dto.getCommitteeName())
                .processResult(dto.getGeneralResult())
                .billStatus(dto.getProcStageCn())
                .summary(dto.getSummary())
                .representativeName(parseRepresentativeName(dto.getProposer()))
                .status("ACTIVE")
                .build();
    }

    private void update(Bill bill, BillInfoResponseDto.ItemDto dto) {
        // 업데이트할 필드만 갱신 (여기서는 간단하게 전체 갱신)
        bill.update(
                dto.getBillName(),
                parseDate(dto.getProposeDt()),
                dto.getCommitteeName(),
                dto.getGeneralResult(),
                dto.getProcStageCn(),
                dto.getSummary(),
                parseRepresentativeName(dto.getProposer())
        );
    }

    private String parseRepresentativeName(String proposer) {
        if (proposer == null || proposer.isBlank()) return null;
        return proposer.split(" ")[0].replace("의원", "").trim();
    }

    private LocalDate parseDate(String yyyymmdd) {
        try {
            return LocalDate.parse(yyyymmdd, DATE_FORMAT);
        } catch (Exception e) {
            return null; // 또는 로깅 후 예외 throw
        }
    }
}
