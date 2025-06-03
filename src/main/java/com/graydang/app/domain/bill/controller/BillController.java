package com.graydang.app.domain.bill.controller;

import com.graydang.app.domain.bill.model.dto.BillDetailResponseDto;
import com.graydang.app.domain.bill.service.BillService;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Bill-Controller", description = "Bill 관련 API 엔드포인트")
public class BillController {

    private final BillService billService;

    @GetMapping(value = "/bills/{billId}")
    public BaseResponse<BillDetailResponseDto> getBillDetail(
            @Parameter(description = "조회할 법안의 ID", example = "101")
            @PathVariable("billId") Long billId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("=== Bill Controller getBillDetail 진입 ===");

        BillDetailResponseDto responseDto = billService.getBillDetail(billId, userDetails);
        billService.increaseViewCount(billId);

        return new BaseResponse<>(responseDto);
    }
}
