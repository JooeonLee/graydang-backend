package com.graydang.app.domain.comment.model.dto;

import com.graydang.app.domain.comment.model.Comment;
import com.graydang.app.domain.comment.model.CommentReport;
import com.graydang.app.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 신고 저장 오쳥")
public record CommentReportSaveRequestDto(
        @Schema(
                description = "신고 내용",
                example = "부적절한 표현 입니다."
        )
        String content
) {

    public  CommentReport toEntity(User user, Comment comment) {
        return CommentReport.builder()
                .user(user)
                .comment(comment)
                .reportedUser(comment.getUser())
                .reason(content)
                .status("ACTIVE")
                .build();
    }
}
