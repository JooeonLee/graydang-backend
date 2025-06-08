package com.graydang.app.global.s3.exception;

import com.graydang.app.global.common.exception.BusinessException;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;

public class InvalidFileTypeException extends BusinessException {
    public InvalidFileTypeException(BaseResponseStatus status) {
        super(status);
    }
}
