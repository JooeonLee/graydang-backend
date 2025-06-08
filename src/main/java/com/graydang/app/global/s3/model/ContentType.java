package com.graydang.app.global.s3.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum ContentType {

    JPG("image/jpg", "jpg", "images/"),
    JPEG("image/jpeg", "jpeg", "images/"),
    PNG("image/png", "png", "images/"),
    WEBP("image/webp", "webp", "images/"),
    HEIF("image/heif", "heif", "images/");

    private final String mimeType;
    private final String extension;
    private final String filePath;
    private static final Map<String, ContentType> allowedExtensions;

    static {
        allowedExtensions = new HashMap<>();
        for (ContentType contentType : ContentType.values()) {
            allowedExtensions.put(contentType.getExtension(), contentType);
        }
    }

    public static Optional<ContentType> getByExtension(String extension) {
        return Optional.ofNullable(allowedExtensions.get(extension));
    }
}
