package com.graydang.app.domain.auth.exception;

import com.graydang.app.global.common.exception.BusinessException;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;

public class AuthException extends BusinessException {

    public AuthException(BaseResponseStatus status) {
        super(status);
    }
}
