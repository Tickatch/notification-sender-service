package com.tickatch.notificationsenderservice.sms.domain.exception;

import io.github.tickatch.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * SMS 전송 관련 오류 코드.
 *
 * <p>SMS 전송 처리 과정에서 발생할 수 있는 예외 상황을 정의한다.
 *
 * @author 김형섭
 * @since 1.0.0
 */
@RequiredArgsConstructor
public enum SmsSendErrorCode implements ErrorCode {
  SMS_SEND_UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR.value(), "SMS_SEND_UNKNOWN"),
  SMS_INVALID_API_KEY(HttpStatus.INTERNAL_SERVER_ERROR.value(), "SMS_INVALID_API_KEY"),
  SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "SMS_SEND_FAILED"),
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
