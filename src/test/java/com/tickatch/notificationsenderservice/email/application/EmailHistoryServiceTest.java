package com.tickatch.notificationsenderservice.email.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.tickatch.notificationsenderservice.email.domain.EmailSendHistory;
import com.tickatch.notificationsenderservice.email.domain.EmailSendStatus;
import com.tickatch.notificationsenderservice.global.infrastructure.SendResultEvent;
import com.tickatch.notificationsenderservice.global.infrastructure.SendResultPublisher;
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
class EmailHistoryServiceTest {

  private final EmailHistoryService emailHistoryService;

  private final EmailHistoryQueryService emailHistoryQueryService;

  private final EntityManager em;

  @MockitoBean private final SendResultPublisher sendResultPublisher;

  EmailSendHistory history;

  @BeforeEach
  void setUp() {
    history =
        emailHistoryService.createHistory(
            1L, "search9@example.com", "테스트 이메일", "테스트 이메일입니다.", false);

    em.flush();
    em.clear();
  }

  @Test
  void createHistory() {
    EmailSendHistory history =
        emailHistoryService.createHistory(
            1L, "search9@example.com", "테스트 이메일", "테스트 이메일입니다.", false);

    assertThat(history.getId()).isNotNull();
  }

  @Test
  void markAsSuccess() {
    doNothing().when(sendResultPublisher).publish(any(SendResultEvent.class));

    emailHistoryService.markAsSuccess(history.getId());
    em.flush();
    em.clear();

    EmailSendHistory found = emailHistoryQueryService.find(history.getId());

    assertThat(found.getStatus()).isEqualTo(EmailSendStatus.SUCCESS);
    verify(sendResultPublisher).publish(any(SendResultEvent.class));
  }

  @Test
  void markAsFailed() {
    doNothing().when(sendResultPublisher).publish(any(SendResultEvent.class));

    emailHistoryService.markAsFailed(history.getId(), "failed");
    em.flush();
    em.clear();

    EmailSendHistory found = emailHistoryQueryService.find(history.getId());

    assertThat(found.getErrorMessage()).isEqualTo("failed");
    assertThat(found.getStatus()).isEqualTo(EmailSendStatus.FAILED);
    verify(sendResultPublisher).publish(any(SendResultEvent.class));
  }
}
