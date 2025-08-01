package com.graydang.app.domain.admin.controller;

import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.comment.model.dto.ReportedCommentSummaryResponseDto;
import com.graydang.app.domain.comment.service.CommentReportService;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.common.model.dto.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin-Controller", description = "Admin 관련 API 엔드포인트")
public class AdminController {

    private final CommentReportService commentReportService;

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
}
