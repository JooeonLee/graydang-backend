package com.graydang.app.domain.comment.controller;

import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.comment.model.dto.CommentReadResponseDto;
import com.graydang.app.domain.comment.model.dto.CommentResponseDto;
import com.graydang.app.domain.comment.model.dto.CommentSaveRequestDto;
import com.graydang.app.domain.comment.service.CommentApplicationService;
import com.graydang.app.domain.comment.service.CommentService;
import com.graydang.app.global.common.model.dto.BaseResponse;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Comment-Controller", description = "Comment 관련 API 엔드포인트")
public class CommentController {

    private final CommentService commentService;
    private final CommentApplicationService commentApplicationService;

    @Operation(summary = "의안 상세 보기 화면 댓글 생성", description = "의안에 대한 댓글을 생성합니다.")
    @PostMapping(value = "/bills/{billId}/comments")
    public ResponseEntity<BaseResponse<CommentResponseDto>> createComment(
            @Parameter(description = "댓글을 생성할 의안의 ID", example = "101")
            @PathVariable("billId") Long billId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CommentSaveRequestDto requestDto) {

        long savedCommentId = commentApplicationService.createComment(requestDto, userDetails.getId(), billId);
        CommentResponseDto responseDto = commentApplicationService.getCommentById(savedCommentId);
        return ResponseEntity.ok(BaseResponse.success(responseDto));
    }

    @Operation(summary = "의안에 대한 댓글 조회", description = "의안 상세 화면에서 의안에 대한 댓글 정보를 페이징하여 보여줍니다.")
    @GetMapping(value = "/bills/{billId}/comments")
    public ResponseEntity<BaseResponse<CommentReadResponseDto>> getCommentByBillId(
            @Parameter(description = "댓글을 조회할 의안의 ID", example = "101")
            @PathVariable("billId") Long billId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "16") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);

        CommentReadResponseDto responseDto = commentApplicationService.getCommentByBillId(customUserDetails, billId, pageRequest);
        return ResponseEntity.ok(BaseResponse.success(responseDto));
    }

    @Operation(summary = "의안에 대한 댓글 수정", description = "의안에 대한 댓글을 수정합니다.")
    @PatchMapping(value = "/comments/{commentId}")
    public ResponseEntity<BaseResponse<Long>> updateComment(
            @Parameter(description = "댓글 ID", example = "1")
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CommentSaveRequestDto requestDto
    ) {
        long updatedCommentId = commentService.updateComment(commentId, userDetails.getId(), requestDto);
        return ResponseEntity.ok(BaseResponse.success(updatedCommentId));
    }

    @Operation(summary = "의안에 대한 댓글 삭제", description = "의안에 대한 댓글을 삭제합니다.")
    @DeleteMapping(value = "/comments/{commentId}")
    public ResponseEntity<BaseResponse<Long>> deleteComment(
            @Parameter(description = "댓글 ID", example = "1")
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        long deletedCommentId = commentService.deleteComment(commentId, userDetails.getId());
        return ResponseEntity.ok(BaseResponse.success(deletedCommentId));
    }
}
