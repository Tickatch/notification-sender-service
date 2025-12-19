package com.tickatch.notificationsenderservice.slack.infrastructure;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.tickatch.notificationsenderservice.slack.domain.dto.SlackChannelSendRequest;
import com.tickatch.notificationsenderservice.slack.domain.dto.SlackDmSendRequest;
import com.tickatch.notificationsenderservice.slack.domain.exception.SlackSendException;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackConversationOpenRequest;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackMessageRequest;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackMessageResponse;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackOpenChannelResponse;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackOpenChannelResponse.SlackChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SlackMessageSenderTest {
  @Mock private SlackFeignClient slackFeignClient;

  @InjectMocks private SlackMessageSender slackMessageSender;

  @Test
  void sendDirectMessage() {
    SlackOpenChannelResponse openResponse =
        new SlackOpenChannelResponse(true, null, new SlackChannel("D67890"));
    given(slackFeignClient.openDirectMessageChannel(any(SlackConversationOpenRequest.class)))
        .willReturn(openResponse);

    SlackMessageResponse messageResponse = new SlackMessageResponse(true, null);
    given(slackFeignClient.sendMessageToChannel(any(SlackMessageRequest.class)))
        .willReturn(messageResponse);

    SlackDmSendRequest request = new SlackDmSendRequest("U12345", "Hello!");

    assertThatCode(() -> slackMessageSender.sendDirectMessage(request)).doesNotThrowAnyException();
  }

  @Test
  void sendDirectMessageIfOpenChannelFailed() {
    SlackOpenChannelResponse openResponse =
        new SlackOpenChannelResponse(false, "missing_scope", null);
    given(slackFeignClient.openDirectMessageChannel(any())).willReturn(openResponse);

    SlackDmSendRequest request = new SlackDmSendRequest("U12345", "Hello!");

    assertThatThrownBy(() -> slackMessageSender.sendDirectMessage(request))
        .isInstanceOf(SlackSendException.class);
  }

  @Test
  void sendDirectMessageIfMessageSendFailed() {
    SlackOpenChannelResponse openResponse =
        new SlackOpenChannelResponse(true, null, new SlackChannel("D67890"));
    given(slackFeignClient.openDirectMessageChannel(any(SlackConversationOpenRequest.class)))
        .willReturn(openResponse);

    SlackMessageResponse messageResponse = new SlackMessageResponse(false, "channel_not_found");
    given(slackFeignClient.sendMessageToChannel(any())).willReturn(messageResponse);

    SlackDmSendRequest request = new SlackDmSendRequest("U12345", "Hello!");

    assertThatThrownBy(() -> slackMessageSender.sendDirectMessage(request))
        .isInstanceOf(SlackSendException.class);
  }

  @Test
  void sendChannelMessage() {
    SlackMessageResponse response = new SlackMessageResponse(true, null);
    given(slackFeignClient.sendMessageToChannel(any())).willReturn(response);

    SlackChannelSendRequest request = new SlackChannelSendRequest("C12345", "Alert!");

    assertThatCode(() -> slackMessageSender.sendChannelMessage(request)).doesNotThrowAnyException();
  }

  @Test
  void sendChannelMessageIfMessageSendFailed() {
    SlackMessageResponse response = new SlackMessageResponse(false, "not_in_channel");
    given(slackFeignClient.sendMessageToChannel(any())).willReturn(response);

    SlackChannelSendRequest request = new SlackChannelSendRequest("C12345", "Alert!");

    assertThatThrownBy(() -> slackMessageSender.sendChannelMessage(request))
        .isInstanceOf(SlackSendException.class);
  }
}
