package com.graydang.app.domain.comment.repository.projection;

import java.time.LocalDateTime;

public interface CommentSimpleProjection {

    Long getCommentId();
    String getContent();
    LocalDateTime getCreatedAt();
    Long getLikeCount();
    String getTitle();
}
