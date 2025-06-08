package com.graydang.app.global.s3.model;

import lombok.Getter;

@Getter
public enum ImagePrefix {

    USER("user/"),
    TEST("test/"),
    DEFAULT("default/");

    ImagePrefix(String prefix) {
        this.value = prefix;
    }

    private final String value;
}
