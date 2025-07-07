package com.graydang.app.domain.comment.model.dto;

import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.comment.model.Comment;
import com.graydang.app.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.ArrayList;

@Schema(description = "댓글 저장 요청")
public record CommentSaveRequestDto(
        @Schema(
                description = "댓글 내용",
                example = "좋은 의안이네요!"
        )
        String content
) {

    public Comment toEntity(User user, Bill bill) {
        return Comment.builder()
                .user(user)
                .bill(bill)
                .content(content)
                .isEdited(false)
                .status("ACTIVE")
                .likes(new ArrayList<>())
                .reports(new ArrayList<>())
                .build();
    }
}
