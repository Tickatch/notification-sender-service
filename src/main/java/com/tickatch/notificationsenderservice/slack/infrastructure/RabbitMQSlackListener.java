package com.tickatch.notificationsenderservice.slack.infrastructure;

import com.tickatch.notificationsenderservice.global.infrastructure.RabbitMQConfig;
import com.tickatch.notificationsenderservice.slack.application.SlackHistoryService;
import com.tickatch.notificationsenderservice.slack.domain.SlackSendHistory;
import com.tickatch.notificationsenderservice.slack.domain.SlackSender;
import com.tickatch.notificationsenderservice.slack.domain.dto.SlackChannelSendRequest;
import com.tickatch.notificationsenderservice.slack.domain.exception.SlackSendException;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackChannelMessageSendRequestEvent;
import io.github.tickatch.common.event.EventContext;
import io.github.tickatch.common.event.IntegrationEvent;
import io.github.tickatch.common.message.MessageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQSlackListener {

  private final SlackHistoryService slackHistoryService;

  private final SlackSender slackSender;

  private final MessageResolver messageResolver;

  @RabbitListener(queues = RabbitMQConfig.QUEUE_SLACK)
  public void channelMessageSendRequest(IntegrationEvent request) {
    EventContext.run(request, this::sendChannelMessageAndRecordHistory);
  }

  private void sendChannelMessageAndRecordHistory(IntegrationEvent event) {
    SlackChannelMessageSendRequestEvent payload =
        event.getPayloadAs(SlackChannelMessageSendRequestEvent.class);

    log.info("Slack 채널 메세지 전송 요청 수신 - Slack 채널[{}] 메세지 전송 시작", payload.getChannelId());

    SlackSendHistory history =
        slackHistoryService.createChannelMessageHistory(
            payload.getChannelId(), payload.getMessage());

    send(payload, history.getId());

    slackHistoryService.markAsSuccess(history.getId());
  }

  private void send(SlackChannelMessageSendRequestEvent payload, Long historyId) {
    try {
      slackSender.sendChannelMessage(
          new SlackChannelSendRequest(payload.getChannelId(), payload.getMessage()));

      log.info("Slack 채널[{}] 메세지 전송 완료", payload.getChannelId());
    } catch (SlackSendException e) {
      log.error("Slack 채널[{}] 메세지 전송 실패", payload.getChannelId(), e);

      slackHistoryService.markAsFailed(
          historyId, messageResolver.resolve(e.getCode(), e.getErrorArgs()));

      throw e;
    }
  }
}
