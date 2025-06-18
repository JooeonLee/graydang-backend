package com.graydang.app.domain.bill.exception;

import com.graydang.app.global.common.exception.BusinessException;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;

public class BillReactionException extends BusinessException {
    public BillReactionException(BaseResponseStatus status) {
        super(status);
    }
}
