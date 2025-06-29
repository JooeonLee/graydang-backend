package com.graydang.app.domain.bill.controller;

import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.service.BillScrapeService;
import com.graydang.app.domain.bill.service.BillService;
import com.graydang.app.global.common.model.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Bill-Scrape-Controller", description = "Bill Scrape 관련 API 엔드포인트")
public class BillScrapeController {

    private final BillScrapeService billScrapeService;
    private final BillService billService;

    @Operation(summary = "의안 스크랩 토글", description = "의안에 대한 스크랩을 토글합니다.")
    @PostMapping("/bills/{billId}/scraps/toggle")
    public ResponseEntity<BaseResponse<Boolean>> toggleBillScrape(
            @Parameter(description = "스크랩 토글을 시도할 의안의 ID", example = "101")
            @PathVariable("billId") Long billId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getUser().getId();
        Bill bill = billService.findByIdOrThrow(billId);
        boolean scraped = billScrapeService.toggleScrape(userId, bill);

        return ResponseEntity.ok(BaseResponse.success(scraped));
    }
}
