package com.graydang.app.domain.bill.exception.handler;

import com.graydang.app.domain.bill.exception.BillReactionException;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class BillReactionExceptionControllerAdvice {

    @ExceptionHandler(BillReactionException.class)
    public ResponseEntity<BaseResponse<Void>> handleBillReactionException(BillReactionException e) {
        log.warn("Received BillReactionException: ", e);
        BaseResponseStatus status = e.getBaseResponseStatus();

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.failure(status));
    }
}
