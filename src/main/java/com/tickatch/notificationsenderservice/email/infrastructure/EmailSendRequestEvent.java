package com.tickatch.notificationsenderservice.email.infrastructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tickatch.common.event.DomainEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class EmailSendRequestEvent extends DomainEvent {

  private final String email;

  private final String subject;

  private final String content;

  private final boolean isHtml;

  public EmailSendRequestEvent(String email, String content, String subject, boolean isHtml) {
    super();
    this.email = email;
    this.subject = this.content = content;
    this.isHtml = isHtml;
  }

  @JsonCreator
  public EmailSendRequestEvent(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("email") String email,
      @JsonProperty("subject") String subject,
      @JsonProperty("content") String content,
      @JsonProperty("isHtml") boolean isHtml) {
    super(eventId, occurredAt, version);
    this.email = email;
    this.subject = subject;
    this.content = content;
    this.isHtml = isHtml;
  }
}
