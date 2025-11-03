package com.graydang.app.domain.comment.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommentStatus {
    ACTIVE("활성"),
    DELETED("삭제됨");

    private final String description;
}