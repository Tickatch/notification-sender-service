package com.tickatch.notificationsenderservice.slack.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickatch.notificationsenderservice.slack.application.SlackHistoryService;
import com.tickatch.notificationsenderservice.slack.domain.SlackSendHistory;
import com.tickatch.notificationsenderservice.slack.domain.SlackSender;
import com.tickatch.notificationsenderservice.slack.domain.dto.SlackChannelSendRequest;
import com.tickatch.notificationsenderservice.slack.domain.exception.SlackSendErrorCode;
import com.tickatch.notificationsenderservice.slack.domain.exception.SlackSendException;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackChannelMessageSendRequestEvent;
import io.github.tickatch.common.event.IntegrationEvent;
import io.github.tickatch.common.message.MessageResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RabbitMQSlackListenerTest {

  @Mock private SlackSender slackSender;

  @Mock private SlackHistoryService slackHistoryService;

  @InjectMocks private RabbitMQSlackListener rabbitMQSlackListener;

  @Mock private IntegrationEvent integrationEvent;

  @Mock private MessageResolver messageResolver;

  @Test
  void channelMessageSendRequest() {
    SlackChannelMessageSendRequestEvent payload =
        new SlackChannelMessageSendRequestEvent(1L, "C12345678", "테스트 메세지");
    when(integrationEvent.getPayloadAs(SlackChannelMessageSendRequestEvent.class))
        .thenReturn(payload);

    SlackSendHistory history =
        SlackSendHistory.createChannel(
            payload.getNotificationId(), payload.getChannelId(), payload.getMessage());
    ReflectionTestUtils.setField(history, "id", 1L);
    when(slackHistoryService.createChannelMessageHistory(anyLong(), anyString(), anyString()))
        .thenReturn(history);

    doNothing().when(slackSender).sendChannelMessage(any((SlackChannelSendRequest.class)));
    doNothing().when(slackHistoryService).markAsSuccess(anyLong());

    rabbitMQSlackListener.channelMessageSendRequest(integrationEvent);

    verify(slackSender).sendChannelMessage(any((SlackChannelSendRequest.class)));
    verify(slackHistoryService).markAsSuccess(anyLong());
  }

  @Test
  void channelMessageSendRequestIfFailed() {
    SlackChannelMessageSendRequestEvent payload =
        new SlackChannelMessageSendRequestEvent(1L, "C12345678", "테스트 메세지");
    when(integrationEvent.getPayloadAs(SlackChannelMessageSendRequestEvent.class))
        .thenReturn(payload);

    SlackSendHistory history =
        SlackSendHistory.createChannel(
            payload.getNotificationId(), payload.getChannelId(), payload.getMessage());
    ReflectionTestUtils.setField(history, "id", 1L);
    when(slackHistoryService.createChannelMessageHistory(anyLong(), anyString(), anyString()))
        .thenReturn(history);

    doThrow(new SlackSendException(SlackSendErrorCode.SLACK_SEND_FAILED, "실패"))
        .when(slackSender)
        .sendChannelMessage(any((SlackChannelSendRequest.class)));
    doNothing().when(slackHistoryService).markAsFailed(anyLong(), anyString());
    when(messageResolver.resolve(any(), any())).thenReturn("에러 메시지");

    rabbitMQSlackListener.channelMessageSendRequest(integrationEvent);

    verify(slackSender).sendChannelMessage(any((SlackChannelSendRequest.class)));
    verify(slackHistoryService).markAsFailed(anyLong(), anyString());
  }
}
