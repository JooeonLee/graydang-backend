package com.graydang.app.domain.admin.controller;

import com.graydang.app.domain.admin.model.dto.BillUpdateRequestDto;
import com.graydang.app.domain.admin.service.AdminBillService;
import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/bills")
@RequiredArgsConstructor
@Tag(name = "Admin-Bill-Controller", description = "법안 관리 API 엔드포인트")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBillController {

    private final AdminBillService adminBillService;

    @Operation(summary = "법안 정보 수정", description = "관리자가 법안 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "법안 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "법안을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PutMapping("/{billId}")
    public ResponseEntity<BaseResponse<Void>> updateBill(
            @Parameter(description = "수정할 법안 ID", example = "1")
            @PathVariable Long billId,
            @Valid @RequestBody BillUpdateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("=== Admin Bill Controller updateBill 진입 - billId: {}, adminId: {} ===", 
                billId, userDetails.getId());
        
        adminBillService.updateBill(billId, requestDto, userDetails.getId());
        
        return ResponseEntity.ok(BaseResponse.success(BaseResponseStatus.SUCCESS));
    }
}