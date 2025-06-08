package com.graydang.app.global.s3.model.dto;

import com.graydang.app.global.s3.model.ContentType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.InputStream;

@Getter
@NoArgsConstructor
public class S3File {

    private String filename;
    private ContentType contentType;
    private Long contentLength;
    private InputStream inputStream;
    private String objectUrl;

    @Builder
    public S3File(String filename, ContentType contentType, Long contentLength, InputStream inputStream, String objectUrl) {
        this.filename = filename;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.inputStream = inputStream;
        this.objectUrl = objectUrl;
    }

    public S3File putObjectUrl(String objectUrl) {
        this.objectUrl = objectUrl;
        return this;
    }
}
