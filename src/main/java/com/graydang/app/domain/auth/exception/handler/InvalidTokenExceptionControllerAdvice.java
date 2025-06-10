package com.graydang.app.domain.auth.exception.handler;

import com.graydang.app.domain.auth.exception.InvalidTokenException;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class InvalidTokenExceptionControllerAdvice {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidTokenException(InvalidTokenException e) {
        log.warn("InvalidTokenException 발생: {}", e.getMessage());
        BaseResponseStatus status = e.getBaseResponseStatus();

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.failure(status));
    }
}
