package com.graydang.app.domain.comment.controller;

import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.comment.service.CommentLikeService;
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
@Tag(name = "Comment-Like-Controller", description = "Comment Like 관련 API 엔드포인트")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @Operation(summary = "댓글에 대한 좋아요 토글", description = "댓글에 대한 좋아요를 토글합니다.")
    @PostMapping("/comments/{commentId}/likes/toggle")
    public ResponseEntity<BaseResponse<Boolean>> toggleCommentLike(
            @Parameter(description = "좋아요 토글을 시도할 댓글의 ID", example = "1")
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getUser().getId();
        boolean liked = commentLikeService.toggleLike(userId, commentId);

        return ResponseEntity.ok(BaseResponse.success(liked));
    }
}
