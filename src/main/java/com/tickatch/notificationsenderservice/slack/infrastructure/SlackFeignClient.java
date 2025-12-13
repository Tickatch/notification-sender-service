package com.tickatch.notificationsenderservice.slack.infrastructure;

import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackConversationOpenRequest;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackMessageRequest;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackMessageResponse;
import com.tickatch.notificationsenderservice.slack.infrastructure.dto.SlackOpenChannelResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "slack", url = "https://slack.com/api", configuration = SlackFeignConfig.class)
public interface SlackFeignClient {
  @PostMapping(value = "/conversations.open")
  SlackOpenChannelResponse openDirectMessageChannel(
      @RequestBody SlackConversationOpenRequest request);

  @PostMapping(value = "/chat.postMessage")
  SlackMessageResponse sendMessageToChannel(@RequestBody SlackMessageRequest request);
}
