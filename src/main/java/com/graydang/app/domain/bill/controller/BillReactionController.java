package com.graydang.app.domain.bill.controller;

import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.dto.BillReactionCountResponseDto;
import com.graydang.app.domain.bill.model.dto.BillReactionRequestDto;
import com.graydang.app.domain.bill.model.dto.BillReactionResponseDto;
import com.graydang.app.domain.bill.service.BillReactionService;
import com.graydang.app.domain.bill.service.BillService;
import com.graydang.app.global.common.model.dto.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Bill-Reaction-Controller", description = "Bill Reaction 관련 API 엔드포인트")
public class BillReactionController {

    private final BillReactionService billReactionService;
    private final BillService billService;

    @PostMapping("/bills/{billId}/reactions")
    public ResponseEntity<BaseResponse<BillReactionResponseDto>> toggleBillReaction(
            @PathVariable("billId") Long billId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody BillReactionRequestDto requestDto) {

        Long userId = customUserDetails.getUser().getId();
        Bill bill = billService.findByIdOrThrow(billId);
        BillReactionResponseDto responseDto = billReactionService.toggleReaction(userId, bill, requestDto);

        return ResponseEntity.ok(BaseResponse.success(responseDto));
    }

    @GetMapping("/bills/{billId}/reactions")
    public ResponseEntity<BaseResponse<BillReactionCountResponseDto>> getBillReactionCount(
            @PathVariable("billId") Long billId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Bill bill = billService.findByIdOrThrow(billId);
        Long userId = userDetails != null ? userDetails.getUser().getId() : null;
        BillReactionCountResponseDto responseDto = billReactionService.getBillReactionCount(bill, userId);

        return ResponseEntity.ok(BaseResponse.success(responseDto));
    }
}
