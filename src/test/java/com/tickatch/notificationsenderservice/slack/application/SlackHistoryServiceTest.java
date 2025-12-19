package com.tickatch.notificationsenderservice.slack.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.tickatch.notificationsenderservice.global.infrastructure.SendResultEvent;
import com.tickatch.notificationsenderservice.global.infrastructure.SendResultPublisher;
import com.tickatch.notificationsenderservice.slack.domain.SlackSendHistory;
import com.tickatch.notificationsenderservice.slack.domain.SlackSendStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@RequiredArgsConstructor
class SlackHistoryServiceTest {
  private final SlackHistoryService slackHistoryService;

  private final SlackHistoryQueryService slackHistoryQueryService;

  private final EntityManager em;

  @MockitoBean private final SendResultPublisher sendResultPublisher;

  SlackSendHistory history;

  @BeforeEach
  void setUp() {
    history = slackHistoryService.createDmHistory(1L, "U12345678", "테스트 메시지");

    em.flush();
    em.clear();
  }

  @Test
  void createDmHistory() {
    SlackSendHistory history = slackHistoryService.createDmHistory(1L, "U12345678", "테스트 메시지");

    assertThat(history.getId()).isNotNull();
    assertThat(history.getSlackUserId()).isEqualTo("U12345678");
  }

  @Test
  void createChannelMessageHistory() {
    SlackSendHistory history =
        slackHistoryService.createChannelMessageHistory(1L, "C12345678", "테스트 메시지");

    assertThat(history.getId()).isNotNull();
    assertThat(history.getChannelId()).isEqualTo("C12345678");
  }

  @Test
  void markAsSuccess() {
    doNothing().when(sendResultPublisher).publish(any(SendResultEvent.class));

    slackHistoryService.markAsSuccess(history.getId());
    em.flush();
    em.clear();

    SlackSendHistory found = slackHistoryQueryService.find(history.getId());

    assertThat(found.getStatus()).isEqualTo(SlackSendStatus.SUCCESS);
    assertThat(found.getSentAt()).isNotNull();
    verify(sendResultPublisher).publish(any(SendResultEvent.class));
  }

  @Test
  void markAsFailed() {
    doNothing().when(sendResultPublisher).publish(any(SendResultEvent.class));

    slackHistoryService.markAsFailed(history.getId(), "failed");
    em.flush();
    em.clear();

    SlackSendHistory found = slackHistoryQueryService.find(history.getId());

    assertThat(found.getErrorMessage()).isEqualTo("failed");
    assertThat(found.getStatus()).isEqualTo(SlackSendStatus.FAILED);
    verify(sendResultPublisher).publish(any(SendResultEvent.class));
  }
}
