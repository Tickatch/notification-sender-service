package com.tickatch.notificationsenderservice.slack.infrastructure.dto;

import lombok.Getter;

@Getter
public class SlackOpenChannelResponse extends AbstractSlackResponse {
  private final SlackChannel channel;

  public SlackOpenChannelResponse(boolean ok, String error, SlackChannel channel) {
    super(ok, error);
    this.channel = channel;
  }

  public record SlackChannel(String id) {}
}
