package com.tickatch.notificationsenderservice.slack.infrastructure;

import com.tickatch.notificationsenderservice.slack.domain.SlackChanelSendRequest;
import com.tickatch.notificationsenderservice.slack.domain.SlackDmSendRequest;
import com.tickatch.notificationsenderservice.slack.domain.SlackSender;
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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackMessageSender implements SlackSender {

  private final SlackFeignClient slackFeignClient;

  private static final String UNKNOWN_ERROR = "Unknown error occurred";

  public void sendDirectMessage(SlackDmSendRequest request) {
    log.info("Slack DM 발송 시작: slackId={}", request.slackId());

    String dmChannelId = openDirectMessageChannel(request.slackId());

    sendMessageToChannel(dmChannelId, request.message());

    log.info("Slack DM 발송 성공: slackId={}, channelId={}", request.slackId(), dmChannelId);
  }

  @Override
  public void sendChanelMessage(SlackChanelSendRequest request) {
    log.info("Slack 채널 메시지 발송 시작: channelId={}", request.chanelId());

    sendMessageToChannel(request.chanelId(), request.message());

    log.info("Slack 채널 메시지 발송 성공: channelId={}", request.chanelId());
  }

  private String openDirectMessageChannel(String userId) {
    SlackOpenChannelResponse response =
        slackFeignClient.openDirectMessageChannel(
            new SlackConversationOpenRequest(List.of(userId)));

    validateResponse(response, SlackSendErrorCode.SLACK_CHANEL_CREATION_FAILED);

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
