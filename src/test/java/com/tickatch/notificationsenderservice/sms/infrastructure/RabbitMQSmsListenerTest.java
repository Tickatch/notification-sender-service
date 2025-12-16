package com.tickatch.notificationsenderservice.sms.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickatch.notificationsenderservice.sms.application.SmsHistoryService;
import com.tickatch.notificationsenderservice.sms.domain.SmsSendHistory;
import com.tickatch.notificationsenderservice.sms.domain.SmsSender;
import com.tickatch.notificationsenderservice.sms.domain.dto.SmsSendRequest;
import com.tickatch.notificationsenderservice.sms.domain.exception.SmsSendErrorCode;
import com.tickatch.notificationsenderservice.sms.domain.exception.SmsSendException;
import io.github.tickatch.common.event.IntegrationEvent;
import io.github.tickatch.common.message.MessageResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RabbitMQSmsListenerTest {

  @Mock private SmsSender smsSender;

  @Mock private SmsHistoryService smsHistoryService;

  @Mock private IntegrationEvent integrationEvent;

  @Mock private MessageResolver messageResolver;

  @InjectMocks private RabbitMQSmsListener rabbitMQSmsListener;

  @Test
  void SmsSendRequest() {
    SmsSendRequestEvent payload = new SmsSendRequestEvent("01012345678", "테스트 메세지");
    when(integrationEvent.getPayloadAs(SmsSendRequestEvent.class)).thenReturn(payload);

    SmsSendHistory history = SmsSendHistory.create(payload.getPhoneNumber(), payload.getMessage());
    ReflectionTestUtils.setField(history, "id", 1L);
    when(smsHistoryService.createHistory(anyString(), anyString())).thenReturn(history);

    when(smsSender.send(any(SmsSendRequest.class))).thenReturn("OK");
    doNothing().when(smsHistoryService).markAsSuccess(anyLong(), anyString());

    rabbitMQSmsListener.SmsSendRequest(integrationEvent);

    verify(smsSender).send(any((SmsSendRequest.class)));
    verify(smsHistoryService).markAsSuccess(anyLong(), anyString());
  }

  @Test
  void smsSendRequestIfFailed() {
    SmsSendRequestEvent payload = new SmsSendRequestEvent("01012345678", "테스트 메세지");
    when(integrationEvent.getPayloadAs(SmsSendRequestEvent.class)).thenReturn(payload);

    SmsSendHistory history = SmsSendHistory.create(payload.getPhoneNumber(), payload.getMessage());
    ReflectionTestUtils.setField(history, "id", 1L);
    when(smsHistoryService.createHistory(anyString(), anyString())).thenReturn(history);

    doThrow(new SmsSendException(SmsSendErrorCode.SMS_SEND_FAILED))
        .when(smsSender)
        .send(any(SmsSendRequest.class));
    doNothing().when(smsHistoryService).markAsFailed(anyLong(), anyString());
    when(messageResolver.resolve(any())).thenReturn("에러 메시지");

    assertThatThrownBy(() -> rabbitMQSmsListener.SmsSendRequest(integrationEvent))
        .isInstanceOf(SmsSendException.class);
    verify(smsSender).send(any(SmsSendRequest.class));
    verify(smsHistoryService).markAsFailed(anyLong(), anyString());
  }
}
