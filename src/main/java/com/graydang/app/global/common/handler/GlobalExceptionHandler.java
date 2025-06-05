package com.graydang.app.global.common.handler;

import com.graydang.app.global.common.model.dto.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.graydang.app.global.common.model.enums.BaseResponseStatus.HTTP_METHOD_TYPE_MISMATCH;
import static com.graydang.app.global.common.model.enums.BaseResponseStatus.METHOD_ARGUMENT_TYPE_MISMATCH;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public BaseResponse<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {

        JSONObject result = new JSONObject();
        result.put(e.getName(), e.getMessage());
        return new BaseResponse<>(METHOD_ARGUMENT_TYPE_MISMATCH, result);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public BaseResponse<?> handleMethodNotAllowedException(HttpRequestMethodNotSupportedException e) {

        JSONObject result = new JSONObject();
        result.put(e.getMethod(), e.getMessage());
        return new BaseResponse<>(HTTP_METHOD_TYPE_MISMATCH, result);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        Map<String, Object> errorMap = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(METHOD_ARGUMENT_TYPE_MISMATCH, errorMap));

    }
}
