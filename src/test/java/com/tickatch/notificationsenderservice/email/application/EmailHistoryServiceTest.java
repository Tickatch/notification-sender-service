package com.tickatch.notificationsenderservice.email.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickatch.notificationsenderservice.email.domain.EmailSendHistory;
import com.tickatch.notificationsenderservice.email.domain.EmailSendStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@RequiredArgsConstructor
class EmailHistoryServiceTest {

  private final EmailHistoryService emailHistoryService;

  private final EmailHistoryQueryService emailHistoryQueryService;

  private final EntityManager em;

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
    emailHistoryService.markAsSuccess(history.getId());
    em.flush();
    em.clear();

    EmailSendHistory found = emailHistoryQueryService.find(history.getId());

    assertThat(found.getStatus()).isEqualTo(EmailSendStatus.SUCCESS);
  }

  @Test
  void markAsFailed() {
    emailHistoryService.markAsFailed(history.getId(), "failed");
    em.flush();
    em.clear();

    EmailSendHistory found = emailHistoryQueryService.find(history.getId());

    assertThat(found.getErrorMessage()).isEqualTo("failed");
    assertThat(found.getStatus()).isEqualTo(EmailSendStatus.FAILED);
  }
}
