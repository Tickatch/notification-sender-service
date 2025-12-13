package com.tickatch.notificationsenderservice.slack.infrastructure.dto;

import java.util.List;

public record SlackConversationOpenRequest(List<String> users) {
  public SlackConversationOpenRequest {
    users = List.copyOf(users);
  }
}
