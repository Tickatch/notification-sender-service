package com.tickatch.notificationsenderservice.slack.domain;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SlackSendHistoryTest {

  SlackSendHistory history;

  @BeforeEach
  void setUp() {
    history = SlackSendHistory.createDm("U12345678", "테스트 DM");
  }

  @Test
  void createDm() {
    SlackSendHistory history = SlackSendHistory.createDm("U12345678", "테스트 DM");

    assertThat(history.getMessageType()).isEqualTo(SlackMessageType.DM);
    assertThat(history.getSlackUserId()).isEqualTo("U12345678");
    assertThat(history.getMessage()).isEqualTo("테스트 DM");
    assertThat(history.getStatus()).isEqualTo(SlackSendStatus.PENDING);
  }

  @Test
  void createChannel() {
    SlackSendHistory history = SlackSendHistory.createChannel("C12345678", "테스트 채널 메시지");

    assertThat(history.getMessageType()).isEqualTo(SlackMessageType.CHANNEL);
    assertThat(history.getChannelId()).isEqualTo("C12345678");
    assertThat(history.getMessage()).isEqualTo("테스트 채널 메시지");
    assertThat(history.getStatus()).isEqualTo(SlackSendStatus.PENDING);
  }

  @Test
  void markAsSuccess() {
    history.markAsSuccess("success");

    assertThat(history.getStatus()).isEqualTo(SlackSendStatus.SUCCESS);
    assertThat(history.getSenderResponse()).isEqualTo("success");
  }

  @Test
  void markAsFailed() {
    history.markAsFailed("failed");

    assertThat(history.getStatus()).isEqualTo(SlackSendStatus.FAILED);
    assertThat(history.getErrorMessage()).isEqualTo("failed");
  }
}
