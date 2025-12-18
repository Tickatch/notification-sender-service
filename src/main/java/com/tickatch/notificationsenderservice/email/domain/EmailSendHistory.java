package com.tickatch.notificationsenderservice.email.domain;

import com.tickatch.notificationsenderservice.global.domain.AbstractTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_email_send_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailSendHistory extends AbstractTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String emailAddress;

  @Column(nullable = false, length = 500)
  private String subject;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Column(nullable = false)
  private Boolean isHtml;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EmailSendStatus status;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  @Column(columnDefinition = "TEXT")
  private String senderResponse;

  private LocalDateTime sentAt;

  public static EmailSendHistory create(
      String emailAddress, String subject, String content, Boolean isHtml) {
    EmailSendHistory history = new EmailSendHistory();

    history.emailAddress = Objects.requireNonNull(emailAddress);
    history.subject = Objects.requireNonNull(subject);
    history.content = Objects.requireNonNull(content);
    history.isHtml = isHtml;
    history.status = EmailSendStatus.PENDING;

    return history;
  }

  public void markAsSuccess(String senderResponse) {
    this.status = EmailSendStatus.SUCCESS;
    this.sentAt = LocalDateTime.now();
    this.senderResponse = senderResponse;
  }

  public void markAsFailed(String errorMessage) {
    this.status = EmailSendStatus.FAILED;
    this.errorMessage = errorMessage;
  }
}
