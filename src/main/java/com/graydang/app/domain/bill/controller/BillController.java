package com.graydang.app.domain.bill.controller;

import com.graydang.app.domain.bill.model.dto.BillDetailResponseDto;
import com.graydang.app.domain.bill.model.dto.BillRecommendResponseDto;
import com.graydang.app.domain.bill.model.dto.BillSimpleResponseDto;
import com.graydang.app.domain.bill.service.BillApplicationService;
import com.graydang.app.domain.bill.service.BillService;
import com.graydang.app.domain.user.model.InterestKeyword;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.global.common.model.dto.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Bill-Controller", description = "Bill 관련 API 엔드포인트")
public class BillController {

    private final BillService billService;
    private final BillApplicationService billApplicationService;

    @Operation(summary = "의안 상세 보기", description = "의안 상세보기 화면에서 의안 정보를 조회하여 반환합니다.")
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

    @Operation(summary = "홈 화면 실시간 인기 법안", description = "홈 화면에서 법안의 조회수를 기준으로 인기 법안을 페이징하여 보여줍니다.")
    @GetMapping(value = "/bills/popular")
    public ResponseEntity<BaseResponse<SliceResponse<BillSimpleResponseDto>>> getPopularBills(
            @AuthenticationPrincipal CustomUserDetails userDetails,

            @Parameter(description = "요청 페이지(디폴트 값 = 0)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,

            @Parameter(description = "한 페이지당 데이터 수(디폴트 값 = 15)", example = "15")
            @RequestParam(value = "size", required = false, defaultValue = "15") int size) {

        log.info("=== Bill Controller getPopularBills 진입 ===");

        PageRequest pageRequest = PageRequest.of(page, size);

        SliceResponse<BillSimpleResponseDto> responseDto = billService.getPopularBills(userDetails, pageRequest);
        return ResponseEntity.ok(BaseResponse.success(responseDto));
    }

    @Operation(summary = "법안 피드 법안 조회", description = "법안 피드에서 법안을 다양한 키워드 기반으로 검색")
    @GetMapping(value = "/bills")
    public ResponseEntity<BaseResponse<SliceResponse<BillSimpleResponseDto>>> getBillsByKeywordLabels(
            @AuthenticationPrincipal CustomUserDetails userDetails,

            @Parameter(description = "키워드", example = "경제")
            @RequestParam Set<String> keywords,

            @Parameter(description = "요청 페이지(디폴트 값 = 0)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,

            @Parameter(description = "한 페이지당 데이터 수(디폴트 값 = 15)", example = "15")
            @RequestParam(value = "size", required = false, defaultValue = "15") int size,

            @Parameter(description = "정렬 기준(최신순 = proposeDate, 조회순 = viewCount), (디폴트 값 = viewCount)", example = "viewCount")
            @RequestParam(value = "sortBy", required = false, defaultValue = "viewCount") String sortBy) {

        PageRequest pageRequest = PageRequest.of(page, size);

        SliceResponse<BillSimpleResponseDto> responseDto  = billService.getBillsByKeywordLabels(userDetails, keywords, pageRequest, sortBy);
        return ResponseEntity.ok(BaseResponse.success(responseDto));
    }

    @Operation(summary = "추천 법안 조회", description = "홈 화면에서 유저 키워드 기반으로 법안 추천")
    @GetMapping(value = "/bills/recommend")
    public ResponseEntity<BaseResponse<BillRecommendResponseDto>> getBillsByUserKeywords(
            @AuthenticationPrincipal CustomUserDetails userDetails,

            @Parameter(description = "요청 페이지(디폴트 값 = 0)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,

            @Parameter(description = "한 페이지당 데이터 수(디폴트 값 = 15)", example = "15")
            @RequestParam(value = "size", required = false, defaultValue = "15") int size) {

        PageRequest pageRequest = PageRequest.of(page, size);

        BillRecommendResponseDto responseDto = billApplicationService.getBillsByUserKeywords(userDetails, pageRequest);
        return ResponseEntity.ok(BaseResponse.success(responseDto));
    }
}
