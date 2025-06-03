package com.graydang.app.domain.bill.exception.handler;

import com.graydang.app.domain.bill.exception.BillException;
import com.graydang.app.global.common.model.dto.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class BillExceptionControllerAdvice {

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BillException.class)
    public BaseResponse handleBillException(BillException e) {
        log.error("[Handle Bill Exception]", e);
        return new BaseResponse<>(e.getBaseResponseStatus());
    }
}
