package com.graydang.app.global.common.exception;

import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final BaseResponseStatus baseResponseStatus;

    public BusinessException(BaseResponseStatus baseResponseStatus) {
        super(baseResponseStatus.getResponseMessage());
        this.baseResponseStatus = baseResponseStatus;
    }
}
