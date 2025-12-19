package com.tickatch.notificationsenderservice.mobile.infrastructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tickatch.common.event.DomainEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class SmsSendRequestEvent extends DomainEvent {

  private final Long notificationId;

  private final String phoneNumber;

  private final String message;

  public SmsSendRequestEvent(Long notificationId, String phoneNumber, String message) {
    super();
    this.notificationId = notificationId;
    this.phoneNumber = phoneNumber;
    this.message = message;
  }

  @JsonCreator
  public SmsSendRequestEvent(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("notificationId") Long notificationId,
      @JsonProperty("phoneNumber") String phoneNumber,
      @JsonProperty("message") String message) {
    super(eventId, occurredAt, version);
    this.notificationId = notificationId;
    this.phoneNumber = phoneNumber;
    this.message = message;
  }
}
