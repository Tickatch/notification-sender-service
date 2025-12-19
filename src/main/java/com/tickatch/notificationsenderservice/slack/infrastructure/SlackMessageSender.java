package com.tickatch.notificationsenderservice.slack.infrastructure;

import com.tickatch.notificationsenderservice.slack.domain.SlackSender;
import com.tickatch.notificationsenderservice.slack.domain.dto.SlackChannelSendRequest;
import com.tickatch.notificationsenderservice.slack.domain.dto.SlackDmSendRequest;
import com.tickatch.notificationsenderservice.slack.domain.exception.SlackSendErrorCode;
import com.tickatch.notificationsenderservice.slack.domain.exception.SlackSendException;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.AbstractSlackResponse;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackConversationOpenRequest;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackMessageRequest;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackMessageResponse;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackOpenChannelResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackMessageSender implements SlackSender {

  private final SlackFeignClient slackFeignClient;

  private static final String UNKNOWN_ERROR = "Unknown error occurred";

  @Override
  @Retryable(
      retryFor = {SlackSendException.class},
      backoff = @Backoff(delay = 5000, multiplier = 2.0, maxDelay = 10000))
  public void sendDirectMessage(SlackDmSendRequest request) {
    log.info("Slack DM 발송 시작: slackId={}", request.slackId());

    String dmChannelId = openDirectMessageChannel(request.slackId());

    sendMessageToChannel(dmChannelId, request.message());

    log.info("Slack DM 발송 성공: slackId={}, channelId={}", request.slackId(), dmChannelId);
  }

  @Override
  public void sendChannelMessage(SlackChannelSendRequest request) {
    log.info("Slack 채널 메시지 발송 시작: channelId={}", request.channelId());

    sendMessageToChannel(request.channelId(), request.message());

    log.info("Slack 채널 메시지 발송 성공: channelId={}", request.channelId());
  }

  private String openDirectMessageChannel(String userId) {
    SlackOpenChannelResponse response =
        slackFeignClient.openDirectMessageChannel(
            new SlackConversationOpenRequest(List.of(userId)));

    validateResponse(response, SlackSendErrorCode.SLACK_CHANNEL_CREATION_FAILED);

    return response.getChannel().id();
  }

  private void sendMessageToChannel(String channelId, String message) {
    SlackMessageResponse response =
        slackFeignClient.sendMessageToChannel(new SlackMessageRequest(channelId, message));

    validateResponse(response, SlackSendErrorCode.SLACK_SEND_FAILED);
  }

  private void validateResponse(AbstractSlackResponse response, SlackSendErrorCode errorCode) {
    if (response == null) {
      throw new SlackSendException(SlackSendErrorCode.SLACK_SEND_UNKNOWN);
    }

    if (!response.isOk()) {
      throw new SlackSendException(errorCode, extractErrorMessage(response));
    }
  }

  private String extractErrorMessage(AbstractSlackResponse response) {
    return StringUtils.hasText(response.getError()) ? response.getError() : UNKNOWN_ERROR;
  }
}
