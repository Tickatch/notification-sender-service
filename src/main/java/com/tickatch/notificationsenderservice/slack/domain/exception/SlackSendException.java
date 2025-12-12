package com.tickatch.notificationsenderservice.slack.domain.exception;

import io.github.tickatch.common.error.BusinessException;
import io.github.tickatch.common.error.ErrorCode;

/**
 * Slack 메시지 전송 처리 예외.
 *
 * <p>Slack 메시지 전송 도메인에서 발생하는 비즈니스 예외를 표현한다.
 *
 * @author 김형섭
 * @since 1.0.0
 */
public class SlackSendException extends BusinessException {

  /**
   * Slack 메시지 전송 예외 생성자.
   *
   * @param errorCode Slack 메시지 전송 예외 코드
   */
  public SlackSendException(ErrorCode errorCode) {
    super(errorCode);
  }

  /**
   * Slack 메시지 전송 예외 생성자.
   *
   * @param errorCode Slack 메시지 전송 예외 코드
   * @param errorArgs 오류 메시지에 바인딩될 인자 값
   */
  public SlackSendException(ErrorCode errorCode, Object... errorArgs) {
    super(errorCode, errorArgs);
  }

  /**
   * Slack 메시지 전송 예외 생성자.
   *
   * @param errorCode Slack 메시지 전송 예외 코드
   * @param throwable 예외 원인
   */
  public SlackSendException(ErrorCode errorCode, Throwable throwable) {
    super(errorCode, throwable);
  }
}
