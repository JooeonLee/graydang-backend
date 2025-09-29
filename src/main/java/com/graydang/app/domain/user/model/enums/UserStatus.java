package com.graydang.app.domain.user.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE("활성", "정상적으로 활동 중인 사용자"),
    INACTIVE("비활성", "탈퇴한 사용자");

    private final String korean;
    private final String description;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isInactive() {
        return this == INACTIVE;
    }

    public boolean canChangeStatus() {
        return this == ACTIVE;
    }
}