package com.tickatch.notificationsenderservice.email.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickatch.notificationsenderservice.email.application.EmailHistoryService;
import com.tickatch.notificationsenderservice.email.domain.EmailSendHistory;
import com.tickatch.notificationsenderservice.email.domain.EmailSender;
import com.tickatch.notificationsenderservice.email.domain.dto.EmailSendRequest;
import com.tickatch.notificationsenderservice.email.domain.exception.EmailSendErrorCode;
import com.tickatch.notificationsenderservice.email.domain.exception.EmailSendException;
import io.github.tickatch.common.event.IntegrationEvent;
import io.github.tickatch.common.message.MessageResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RabbitMQEmailListenerTest {
  @Mock private EmailSender emailSender;

  @Mock private EmailHistoryService emailHistoryService;

  @Mock private IntegrationEvent integrationEvent;

  @Mock private MessageResolver messageResolver;

  @InjectMocks private RabbitMQEmailListener rabbitMQEmailListener;

  @Test
  void emailSendRequest() {
    EmailSendRequestEvent payload =
        new EmailSendRequestEvent(1L, "test@example.com", "테스트 메세지", "테스트 메세지입니다.", false);
    when(integrationEvent.getPayloadAs(EmailSendRequestEvent.class)).thenReturn(payload);

    EmailSendHistory history =
        EmailSendHistory.create(
            1L, payload.getEmail(), payload.getSubject(), payload.getContent(), payload.isHtml());
    ReflectionTestUtils.setField(history, "id", 1L);
    when(emailHistoryService.createHistory(
            anyLong(), anyString(), anyString(), anyString(), anyBoolean()))
        .thenReturn(history);

    doNothing().when(emailSender).send(any(EmailSendRequest.class));
    doNothing().when(emailHistoryService).markAsSuccess(anyLong());

    rabbitMQEmailListener.emailSendRequest(integrationEvent);

    verify(emailSender).send(any((EmailSendRequest.class)));
    verify(emailHistoryService).markAsSuccess(anyLong());
  }

  @Test
  void emailSendRequestIfFailed() {
    EmailSendRequestEvent payload =
        new EmailSendRequestEvent(1L, "test@example.com", "테스트 메세지", "테스트 메세지입니다.", false);
    when(integrationEvent.getPayloadAs(EmailSendRequestEvent.class)).thenReturn(payload);

    EmailSendHistory history =
        EmailSendHistory.create(
            1L, payload.getEmail(), payload.getSubject(), payload.getContent(), payload.isHtml());
    ReflectionTestUtils.setField(history, "id", 1L);
    when(emailHistoryService.createHistory(
            anyLong(), anyString(), anyString(), anyString(), anyBoolean()))
        .thenReturn(history);

    doThrow(new EmailSendException(EmailSendErrorCode.EMAIL_SEND_FAILED))
        .when(emailSender)
        .send(any(EmailSendRequest.class));
    doNothing().when(emailHistoryService).markAsFailed(anyLong(), anyString());
    when(messageResolver.resolve(any())).thenReturn("에러 메시지");

    assertThatThrownBy(() -> rabbitMQEmailListener.emailSendRequest(integrationEvent))
        .isInstanceOf(EmailSendException.class);
    verify(emailSender).send(any(EmailSendRequest.class));
    verify(emailHistoryService).markAsFailed(anyLong(), anyString());
  }
}
