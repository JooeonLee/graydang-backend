package com.graydang.app.global.common.model.enums;

public record TimeDifference(long value, TimeUnit unit) {

    public String formatted() {
        return unit.format(value);
    }
}
