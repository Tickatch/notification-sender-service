package com.tickatch.notificationsenderservice.sms.domain;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SmsSendHistoryTest {

  SmsSendHistory history;

  @BeforeEach
  void setUp() {
    history = SmsSendHistory.create("01012345678", "테스트 SMS");
  }

  @Test
  void create() {
    SmsSendHistory history = SmsSendHistory.create("01012345678", "테스트 SMS");

    assertThat(history.getStatus()).isEqualTo(SmsSendStatus.PENDING);
  }

  @Test
  void markAsSuccess() {
    history.markAsSuccess("success");

    assertThat(history.getStatus()).isEqualTo(SmsSendStatus.SUCCESS);
    assertThat(history.getSenderResponse()).isEqualTo("success");
    assertThat(history.getSentAt()).isNotNull();
  }

  @Test
  void markAsFailed() {
    history.markAsFailed("failed");

    assertThat(history.getStatus()).isEqualTo(SmsSendStatus.FAILED);
    assertThat(history.getErrorMessage()).isEqualTo("failed");
  }
}
