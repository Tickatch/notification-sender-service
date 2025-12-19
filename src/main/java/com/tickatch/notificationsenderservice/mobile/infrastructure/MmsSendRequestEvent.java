package com.tickatch.notificationsenderservice.mobile.infrastructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tickatch.common.event.DomainEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class MmsSendRequestEvent extends DomainEvent {

  private final Long notificationId;

  private final String phoneNumber;

  private final String message;

  private final String imageBase64;

  public MmsSendRequestEvent(
      Long notificationId, String phoneNumber, String message, String imageBase64) {
    super();
    this.notificationId = notificationId;
    this.phoneNumber = phoneNumber;
    this.message = message;
    this.imageBase64 = imageBase64;
  }

  @JsonCreator
  public MmsSendRequestEvent(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("notificationId") Long notificationId,
      @JsonProperty("phoneNumber") String phoneNumber,
      @JsonProperty("message") String message,
      @JsonProperty("imageBase64") String imageBase64) {
    super(eventId, occurredAt, version);
    this.notificationId = notificationId;
    this.phoneNumber = phoneNumber;
    this.message = message;
    this.imageBase64 = imageBase64;
  }
}
