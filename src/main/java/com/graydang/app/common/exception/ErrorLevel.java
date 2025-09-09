package com.graydang.app.common.exception;

public enum ErrorLevel {
  /** APM Error X, Stacktrace X, Slack X */
  LV1,

  /** APM Error O, Stacktrace O, Slack X */
  /** 백로그에 쌓이듯이 쌓이기만 하면 되는 에러 */
  LV2,

  /** APM O, stacktrace O, Slack O */
  /** 에러로 집계되서 인지는 해야하는 에러 */
  LV3,

  /** LV3 + 에러 발생 시, Slack 에 에러 로그 즉시 발송 */
  /** 즉시 인지해야하는 알람 */
  LV4
}
