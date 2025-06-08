package com.graydang.app.global.s3.exception.handler;

import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import com.graydang.app.global.s3.exception.InvalidFileTypeException;
import com.graydang.app.global.s3.exception.S3FileConvertException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class FileExceptionControllerAdvice {

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidFileTypeException(InvalidFileTypeException e) {
        log.warn("InvalidFileTypeException 발생: {}", e.getMessage());
        BaseResponseStatus status = e.getBaseResponseStatus();

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.failure(status));
    }

    @ExceptionHandler(S3FileConvertException.class)
    public ResponseEntity<BaseResponse<Void>> handleS3FileConvertException(S3FileConvertException e) {
        log.warn("InvalidFileTypeException 발생: {}", e.getMessage());
        BaseResponseStatus status = e.getBaseResponseStatus();

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.failure(status));
    }
}
