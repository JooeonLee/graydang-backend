package com.graydang.app.domain.user.exception.handler;

import com.graydang.app.domain.user.exception.UserException;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class UserExceptionControllerAdvice {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<BaseResponse<Void>> handleUserException(UserException e) {
        log.warn("UserException 발생: {}", e.getMessage());
        BaseResponseStatus status = e.getBaseResponseStatus();

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.failure(status));
    }

}
