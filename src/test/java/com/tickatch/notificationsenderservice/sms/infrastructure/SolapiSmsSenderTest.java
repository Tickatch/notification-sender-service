package com.tickatch.notificationsenderservice.sms.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.tickatch.notificationsenderservice.sms.domain.dto.SmsSendRequest;
import com.tickatch.notificationsenderservice.sms.domain.exception.SmsSendErrorCode;
import com.tickatch.notificationsenderservice.sms.domain.exception.SmsSendException;
import net.nurigo.sdk.message.exception.NurigoBadRequestException;
import net.nurigo.sdk.message.exception.NurigoInvalidApiKeyException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SolapiSmsSenderTest {

  @Mock private DefaultMessageService messageService;

  private SolapiSmsSender smsSender;

  private static final String API_KEY = "test-api-key";
  private static final String API_SECRET = "test-api-secret";
  private static final String DOMAIN = "https://api.solapi.com";
  private static final String FROM_NUMBER = "01012345678";

  SmsSendRequest request;

  @BeforeEach
  void setUp() {
    smsSender = new SolapiSmsSender(API_KEY, API_SECRET, DOMAIN, FROM_NUMBER);
    ReflectionTestUtils.setField(smsSender, "messageService", messageService);
    request = new SmsSendRequest("01098765432", "테스트 메시지입니다.");
  }

  @Test
  void send() {
    SingleMessageSentResponse response = mock(SingleMessageSentResponse.class);
    given(messageService.sendOne(any(SingleMessageSendingRequest.class))).willReturn(response);

    smsSender.send(request);
    verify(messageService, times(1)).sendOne(any(SingleMessageSendingRequest.class));
  }

  @Test
  void sendNurigoBadRequestExceptionThrowsSmsSendException() {
    NurigoBadRequestException exception = new NurigoBadRequestException("Bad request");
    given(messageService.sendOne(any(SingleMessageSendingRequest.class)))
        .willAnswer(
            invocation -> {
              throw exception;
            });

    assertThatThrownBy(() -> smsSender.send(request))
        .isInstanceOf(SmsSendException.class)
        .hasFieldOrPropertyWithValue("errorCode", SmsSendErrorCode.SMS_SEND_FAILED);

    verify(messageService, times(1)).sendOne(any(SingleMessageSendingRequest.class));
  }

  @Test
  void sendNurigoInvalidApiKeyExceptionThrowsSmsSendException() {
    NurigoInvalidApiKeyException exception = new NurigoInvalidApiKeyException("Invalid API key");
    given(messageService.sendOne(any(SingleMessageSendingRequest.class)))
        .willAnswer(
            invocation -> {
              throw exception;
            });

    assertThatThrownBy(() -> smsSender.send(request))
        .isInstanceOf(SmsSendException.class)
        .hasFieldOrPropertyWithValue("errorCode", SmsSendErrorCode.SMS_INVALID_API_KEY);

    verify(messageService, times(1)).sendOne(any(SingleMessageSendingRequest.class));
  }

  @Test
  void sendNurigoUnknownExceptionThrowsSmsSendException() {
    NurigoUnknownException exception = new NurigoUnknownException("Unknown error");
    given(messageService.sendOne(any(SingleMessageSendingRequest.class)))
        .willAnswer(
            invocation -> {
              throw exception;
            });

    assertThatThrownBy(() -> smsSender.send(request))
        .isInstanceOf(SmsSendException.class)
        .hasFieldOrPropertyWithValue("errorCode", SmsSendErrorCode.SMS_SEND_UNKNOWN);

    verify(messageService, times(1)).sendOne(any(SingleMessageSendingRequest.class));
  }

  @Test
  void sendUnexpectedExceptionThrowsSmsSendException() {
    RuntimeException exception = new RuntimeException("예상치 못한 에러");
    given(messageService.sendOne(any(SingleMessageSendingRequest.class))).willThrow(exception);

    assertThatThrownBy(() -> smsSender.send(request))
        .isInstanceOf(SmsSendException.class)
        .hasFieldOrPropertyWithValue("errorCode", SmsSendErrorCode.SMS_SEND_UNKNOWN)
        .hasCause(exception);

    verify(messageService, times(1)).sendOne(any(SingleMessageSendingRequest.class));
  }
}
