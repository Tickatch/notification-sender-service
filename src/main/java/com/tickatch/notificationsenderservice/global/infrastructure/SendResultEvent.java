package com.tickatch.notificationsenderservice.global.infrastructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tickatch.common.event.DomainEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class SendResultEvent extends DomainEvent {

  private static final String AGGREGATE_TYPE = "Notification Sender";
  private static final String ROUTING_KEY = "notification-sender.result";

  private final Long notificationId;
  private final boolean success;
  private final String errorMessage;

  public SendResultEvent(Long notificationId, boolean success, String errorMessage) {
    super();
    this.notificationId = notificationId;
    this.success = success;
    this.errorMessage = errorMessage;
  }

  @JsonCreator
  public SendResultEvent(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("notificationId") Long notificationId,
      @JsonProperty("success") boolean success,
      @JsonProperty("errorMessage") String errorMessage) {
    super(eventId, occurredAt, version);
    this.notificationId = notificationId;
    this.success = success;
    this.errorMessage = errorMessage;
  }

  @Override
  public String getAggregateType() {
    return AGGREGATE_TYPE;
  }

  @Override
  public String getRoutingKey() {
    return ROUTING_KEY;
  }
}
