package com.graydang.app.common.exception;

import lombok.Getter;

public class ApiException extends RuntimeException {

  @Getter private ErrorLevel level;

  @Getter private String errorCode;

  public ApiException(ErrorLevel level, ApiErrorCode errorCode, String message) {
    super(message, null, true, true);
    this.level = level;
    this.errorCode = errorCode.getErrorCode();
  }

  public static ApiException of(ErrorLevel level, ApiErrorCode errorCode, String message) {
    return new ApiException(level, errorCode, message);
  }
}
