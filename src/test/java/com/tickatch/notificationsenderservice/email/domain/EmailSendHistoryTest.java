package com.tickatch.notificationsenderservice.email.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmailSendHistoryTest {

  EmailSendHistory history;

  @BeforeEach
  void setUp() {
    history = EmailSendHistory.create("test@example.com", "테스트 메일", "테스트 메일입니다.", false);
  }

  @Test
  void create() {
    EmailSendHistory history =
        EmailSendHistory.create("test@example.com", "테스트 메일", "테스트 메일입니다.", false);

    assertThat(history.getStatus()).isEqualTo(EmailSendStatus.PENDING);
  }

  @Test
  void markAsSuccess() {
    history.markAsSuccess("success");

    assertThat(history.getStatus()).isEqualTo(EmailSendStatus.SUCCESS);
    assertThat(history.getSentAt()).isNotNull();
    assertThat(history.getSenderResponse()).isEqualTo("success");
  }

  @Test
  void markAsFailed() {
    history.markAsFailed("failed");

    assertThat(history.getStatus()).isEqualTo(EmailSendStatus.FAILED);
    assertThat(history.getErrorMessage()).isEqualTo("failed");
  }
}
