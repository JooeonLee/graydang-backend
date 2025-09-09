package com.graydang.app.common.exception;

import lombok.Getter;

public enum ApiErrorCode {
  BAD_REQUEST("E40000"),
  INVALID_PARAMETER("E40001"),
  DATA_EXISTS("E40002"),
  DATA_NOT_FOUND("E40003");

  @Getter private final String errorCode;

  ApiErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }
}
