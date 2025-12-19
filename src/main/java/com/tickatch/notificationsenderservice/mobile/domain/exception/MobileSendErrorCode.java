package com.tickatch.notificationsenderservice.mobile.domain.exception;

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
public enum MobileSendErrorCode implements ErrorCode {
  MOBILE_INVALID_API_KEY(HttpStatus.INTERNAL_SERVER_ERROR.value(), "MOBILE_INVALID_API_KEY"),
  SMS_SEND_UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR.value(), "SMS_SEND_UNKNOWN"),
  SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "SMS_SEND_FAILED"),
  MMS_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "MMS_IMAGE_UPLOAD_FAILED"),
  MMS_TEMP_FILE_CREATION_FAILED(
      HttpStatus.INTERNAL_SERVER_ERROR.value(), "MMS_TEMP_FILE_CREATION_FAILED"),
  MMS_IMAGE_SIZE_EXCEEDED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "MMS_IMAGE_SIZE_EXCEEDED"),
  MMS_INVALID_IMAGE_FORMAT(HttpStatus.INTERNAL_SERVER_ERROR.value(), "MMS_INVALID_IMAGE_FORMAT"),
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
