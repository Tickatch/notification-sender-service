package com.tickatch.notificationsenderservice.slack.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tickatch.common.event.DomainEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class SlackChannelMessageSendRequestEvent extends DomainEvent {

  private final String channelId;

  private final String message;

  public SlackChannelMessageSendRequestEvent(String channelId, String message) {
    super();
    this.channelId = channelId;
    this.message = message;
  }

  @JsonCreator
  public SlackChannelMessageSendRequestEvent(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("channelId") String channelId,
      @JsonProperty("content") String message) {
    super(eventId, occurredAt, version);
    this.channelId = channelId;
    this.message = message;
  }
}
