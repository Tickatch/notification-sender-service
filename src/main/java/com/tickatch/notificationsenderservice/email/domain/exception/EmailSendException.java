package com.tickatch.notificationsenderservice.email.domain.exception;

import io.github.tickatch.common.error.BusinessException;
import io.github.tickatch.common.error.ErrorCode;

/**
 * 이메일 전송 처리 예외.
 *
 * <p>이메일 전송 도메인에서 발생하는 비즈니스 예외를 표현한다.
 *
 * @author 김형섭
 * @since 1.0.0
 */
public class EmailSendException extends BusinessException {

  /**
   * 이메일 전송 예외 생성자.
   *
   * @param errorCode 이메일 전송 예외 코드
   */
  public EmailSendException(ErrorCode errorCode) {
    super(errorCode);
  }

  /**
   * 이메일 전송 예외 생성자.
   *
   * @param errorCode 이메일 전송 예외 코드
   * @param errorArgs 오류 메시지에 바인딩될 인자 값
   */
  public EmailSendException(ErrorCode errorCode, Object... errorArgs) {
    super(errorCode, errorArgs);
  }

  /**
   * 이메일 전송 예외 생성자.
   *
   * @param errorCode 이메일 전송 예외 코드
   * @param throwable 예외 원인
   */
  public EmailSendException(ErrorCode errorCode, Throwable throwable) {
    super(errorCode, throwable);
  }
}
