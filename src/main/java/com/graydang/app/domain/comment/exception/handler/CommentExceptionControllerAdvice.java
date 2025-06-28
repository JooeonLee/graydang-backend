package com.graydang.app.domain.comment.exception.handler;

import com.graydang.app.domain.comment.exception.CommentException;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CommentExceptionControllerAdvice {

    @ExceptionHandler(CommentException.class)
    public ResponseEntity<BaseResponse<Void>> handleCommentException(CommentException exception) {
        log.warn("CommentException 발생: {}", exception.getMessage());
        BaseResponseStatus status = exception.getBaseResponseStatus();

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.error(status));
    }
}
