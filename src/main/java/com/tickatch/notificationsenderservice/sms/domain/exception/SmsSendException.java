package com.tickatch.notificationsenderservice.sms.domain.exception;

import io.github.tickatch.common.error.BusinessException;
import io.github.tickatch.common.error.ErrorCode;

/**
 * SMS 전송 처리 예외.
 *
 * <p>SMS 전송 도메인에서 발생하는 비즈니스 예외를 표현한다.
 *
 * @author 김형섭
 * @since 1.0.0
 */
public class SmsSendException extends BusinessException {

  /**
   * SMS 전송 예외 생성자.
   *
   * @param errorCode SMS 전송 예외 코드
   */
  public SmsSendException(ErrorCode errorCode) {
    super(errorCode);
  }

  /**
   * SMS 전송 예외 생성자.
   *
   * @param errorCode SMS 전송 예외 코드
   * @param errorArgs 오류 메시지에 바인딩될 인자 값
   */
  public SmsSendException(ErrorCode errorCode, Object... errorArgs) {
    super(errorCode, errorArgs);
  }

  public SmsSendException(ErrorCode errorCode, Throwable throwable) {
    super(errorCode, throwable);
  }
}
