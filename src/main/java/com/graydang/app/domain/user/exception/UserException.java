package com.graydang.app.domain.user.exception;

import com.graydang.app.global.common.exception.BusinessException;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;

public class UserException extends BusinessException {
    public UserException(BaseResponseStatus status) {
        super(status);
    }
}
