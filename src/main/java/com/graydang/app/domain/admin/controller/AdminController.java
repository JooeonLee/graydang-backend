package com.graydang.app.domain.admin.controller;

import com.graydang.app.domain.admin.service.AdminService;
import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.comment.model.dto.ReportedCommentSummaryResponseDto;
import com.graydang.app.domain.comment.service.CommentReportService;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.common.model.dto.SliceResponse;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin-Controller", description = "Admin 관련 API 엔드포인트")
public class AdminController {

    private final CommentReportService commentReportService;
    private final AdminService adminService;

    @Operation(summary = "신고 댓글 조회", description = "신고된 댓글을 조회합니다.")
    @GetMapping(value = "/reports/comments")
    public ResponseEntity<BaseResponse<SliceResponse<ReportedCommentSummaryResponseDto>>> getCommentReports(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "16") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);

        SliceResponse<ReportedCommentSummaryResponseDto> responseDto = commentReportService.getReportedCommentSummary(pageRequest);
        return ResponseEntity.ok(BaseResponse.success(responseDto));
    }

    @Operation(summary = "사용자 차단", description = "관리자가 특정 사용자를 차단합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 차단 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 차단된 사용자, 탈퇴한 사용자 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PutMapping("/users/{userId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> blockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        adminService.blockUser(userId);
        return ResponseEntity.ok(BaseResponse.success(BaseResponseStatus.SUCCESS));
    }

    @Operation(summary = "사용자 차단 해제", description = "관리자가 차단된 사용자의 차단을 해제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 차단 해제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (차단되지 않은 사용자)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PutMapping("/users/{userId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> unblockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        adminService.unblockUser(userId);
        return ResponseEntity.ok(BaseResponse.success(BaseResponseStatus.SUCCESS));
    }
}
