package com.graydang.app.domain.bill.controller;

import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.service.BillScrapeService;
import com.graydang.app.domain.bill.service.BillService;
import com.graydang.app.global.common.model.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BillScrapeController {

    private final BillScrapeService billScrapeService;
    private final BillService billService;

    @PostMapping("/bills/{billId}/scraps/toggle")
    public ResponseEntity<BaseResponse<Boolean>> toggleBillScrape(
            @PathVariable("billId") Long billId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getUser().getId();
        Bill bill = billService.findByIdOrThrow(billId);
        boolean scraped = billScrapeService.toggleScrape(userId, bill);

        return ResponseEntity.ok(BaseResponse.success(scraped));
    }
}
