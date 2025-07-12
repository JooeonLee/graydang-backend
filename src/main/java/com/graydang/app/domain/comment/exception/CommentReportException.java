package com.graydang.app.domain.comment.exception;

import com.graydang.app.global.common.exception.BusinessException;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;

public class CommentReportException extends BusinessException {
    public CommentReportException(BaseResponseStatus status) {
        super(status);
    }
}
