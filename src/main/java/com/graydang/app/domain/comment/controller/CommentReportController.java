package com.graydang.app.domain.comment.controller;

import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.comment.model.dto.CommentReportSaveRequestDto;
import com.graydang.app.domain.comment.service.CommentApplicationService;
import com.graydang.app.domain.comment.service.CommentReportService;
import com.graydang.app.domain.comment.service.CommentService;
import com.graydang.app.global.common.model.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Comment-Report-Controller", description = "Comment Report 관련 API 엔드포인트")
public class CommentReportController {

    private final CommentService commentService;
    private final CommentReportService commentReportService;
    private final CommentApplicationService commentApplicationService;

    @Operation(summary = "댓글 신고하기 생성", description = "댓글에 대한 신고를 생성합니다.")
    @PostMapping(value = "/comments/{commentId}/reports")
    public ResponseEntity<BaseResponse<Long>> createCommentReport(
            @Parameter(description = "신고를 생성할 댓글의 ID", example = "1")
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CommentReportSaveRequestDto requestDto) {

        long savedCommentReportId = commentReportService.reportComment(requestDto, userDetails.getId(), commentId);
        return ResponseEntity.ok(BaseResponse.success(savedCommentReportId));
    }

}
