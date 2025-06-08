package com.graydang.app.global.s3.exception;

import com.graydang.app.global.common.exception.BusinessException;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;

public class S3FileException extends BusinessException {
    public S3FileException(BaseResponseStatus message) {
        super(message);
    }
}
