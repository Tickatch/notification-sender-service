package com.tickatch.notificationsenderservice.slack.domain.exception;

import io.github.tickatch.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Slack 메시지 전송 관련 오류 코드.
 *
 * <p>Slack 메시지 전송 처리 과정에서 발생할 수 있는 예외 상황을 정의한다.
 *
 * @author 김형섭
 * @since 1.0.0
 */
@RequiredArgsConstructor
public enum SlackSendErrorCode implements ErrorCode {
  SLACK_SEND_UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR.value(), "SLACK_SEND_UNKNOWN"),
  SLACK_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "SLACK_SEND_FAILED"),
  SLACK_CHANNEL_CREATION_FAILED(
      HttpStatus.INTERNAL_SERVER_ERROR.value(), "SLACK_CHANNEL_CREATION_FAILED"),
  ;

  private final int status;
  private final String code;

  @Override
  public int getStatus() {
    return this.status;
  }

  @Override
  public String getCode() {
    return this.code;
  }
}
