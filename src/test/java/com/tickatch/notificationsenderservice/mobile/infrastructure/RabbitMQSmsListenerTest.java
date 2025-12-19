package com.tickatch.notificationsenderservice.mobile.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickatch.notificationsenderservice.mobile.application.MobileHistoryService;
import com.tickatch.notificationsenderservice.mobile.domain.MobileSendHistory;
import com.tickatch.notificationsenderservice.mobile.domain.MobileSender;
import com.tickatch.notificationsenderservice.mobile.domain.dto.SmsSendRequest;
import com.tickatch.notificationsenderservice.mobile.domain.exception.MobileSendErrorCode;
import com.tickatch.notificationsenderservice.mobile.domain.exception.MobileSendException;
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

  @Mock private MobileSender mobileSender;

  @Mock private MobileHistoryService mobileHistoryService;

  @Mock private IntegrationEvent integrationEvent;

  @Mock private MessageResolver messageResolver;

  @InjectMocks private RabbitMQSmsListener rabbitMQSmsListener;

  @Test
  void SmsSendRequest() {
    SmsSendRequestEvent payload = new SmsSendRequestEvent(1L, "01012345678", "테스트 메세지");
    when(integrationEvent.getPayloadAs(SmsSendRequestEvent.class)).thenReturn(payload);

    MobileSendHistory history =
        MobileSendHistory.create(
            payload.getNotificationId(), payload.getPhoneNumber(), payload.getMessage());
    ReflectionTestUtils.setField(history, "id", 1L);
    when(mobileHistoryService.createHistory(anyLong(), anyString(), anyString()))
        .thenReturn(history);

    when(mobileSender.send(any(SmsSendRequest.class))).thenReturn("OK");
    doNothing().when(mobileHistoryService).markAsSuccess(anyLong(), anyString());

    rabbitMQSmsListener.smsSendRequest(integrationEvent);

    verify(mobileSender).send(any((SmsSendRequest.class)));
    verify(mobileHistoryService).markAsSuccess(anyLong(), anyString());
  }

  @Test
  void smsSendRequestIfFailed() {
    SmsSendRequestEvent payload = new SmsSendRequestEvent(1L, "01012345678", "테스트 메세지");
    when(integrationEvent.getPayloadAs(SmsSendRequestEvent.class)).thenReturn(payload);

    MobileSendHistory history =
        MobileSendHistory.create(
            payload.getNotificationId(), payload.getPhoneNumber(), payload.getMessage());
    ReflectionTestUtils.setField(history, "id", 1L);
    when(mobileHistoryService.createHistory(anyLong(), anyString(), anyString()))
        .thenReturn(history);

    doThrow(new MobileSendException(MobileSendErrorCode.SMS_SEND_FAILED))
        .when(mobileSender)
        .send(any(SmsSendRequest.class));
    doNothing().when(mobileHistoryService).markAsFailed(anyLong(), anyString());
    when(messageResolver.resolve(any())).thenReturn("에러 메시지");

    rabbitMQSmsListener.smsSendRequest(integrationEvent);

    verify(mobileSender).send(any(SmsSendRequest.class));
    verify(mobileHistoryService).markAsFailed(anyLong(), anyString());
  }
}
