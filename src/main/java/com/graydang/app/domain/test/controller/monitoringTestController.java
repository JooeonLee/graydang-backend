package com.graydang.app.domain.test.controller;

import com.graydang.app.common.exception.ApiErrorCode;
import com.graydang.app.common.exception.ApiException;
import com.graydang.app.common.exception.ErrorLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class monitoringTestController {
  @GetMapping(value = "/slack")
  public ResponseEntity<Object> testSlackError(
  ) {
    try {
      // 여기서는 일부러 예외를 발생시키는 용도
      throw new ApiException(ErrorLevel.LV4, ApiErrorCode.DATA_NOT_FOUND, "test slack error");
    } catch (ApiException e) {
      // 필요하다면 여기서 잡고 로그 남기거나 다른 처리를 할 수도 있음
      throw e; // 그냥 다시 던져서 @ControllerAdvice / @RestControllerAdvice 에서 처리
    }
  }
}
