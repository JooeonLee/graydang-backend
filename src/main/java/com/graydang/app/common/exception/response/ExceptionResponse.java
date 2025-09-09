package com.graydang.app.common.exception.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
public class ExceptionResponse {
  private String code;
  private String message;

  private ExceptionResponse() {
    this.code = "50000";
    this.message = "서비스 내부에 오류가 발생했습니다.";
  }

  private ExceptionResponse(String message) {
    this.code = "50000";
    this.message = message;
  }

  public static ExceptionResponse of(String code, String message) {
    ExceptionResponse exceptionResponse = new ExceptionResponse();
    exceptionResponse.code = code;
    exceptionResponse.message = message;
    return exceptionResponse;
  }
}
