package com.tickatch.notificationsenderservice.mobile.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MobileSendHistoryTest {

  MobileSendHistory history;

  @BeforeEach
  void setUp() {
    history = MobileSendHistory.create(1L, "01012345678", "테스트 SMS");
  }

  @Test
  void create() {
    MobileSendHistory history = MobileSendHistory.create(1L, "01012345678", "테스트 SMS");

    assertThat(history.getStatus()).isEqualTo(MobileSendStatus.PENDING);
  }

  @Test
  void markAsSuccess() {
    history.markAsSuccess("success");

    assertThat(history.getStatus()).isEqualTo(MobileSendStatus.SUCCESS);
    assertThat(history.getSenderResponse()).isEqualTo("success");
    assertThat(history.getSentAt()).isNotNull();
  }

  @Test
  void markAsFailed() {
    history.markAsFailed("failed");

    assertThat(history.getStatus()).isEqualTo(MobileSendStatus.FAILED);
    assertThat(history.getErrorMessage()).isEqualTo("failed");
  }
}
