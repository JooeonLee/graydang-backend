package com.graydang.app.domain.bill.exception;

import com.graydang.app.global.common.exception.BusinessException;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;

public class BillException extends BusinessException {

    public BillException(BaseResponseStatus status) {
        super(status);
    }
}
