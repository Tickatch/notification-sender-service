package com.tickatch.notificationsenderservice.sms.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickatch.notificationsenderservice.sms.domain.SmsSendHistory;
import com.tickatch.notificationsenderservice.sms.domain.SmsSendStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@RequiredArgsConstructor
class SmsHistoryServiceTest {
  private final SmsHistoryService smsHistoryService;

  private final SmsHistoryQueryService smsHistoryQueryService;

  private final EntityManager em;

  SmsSendHistory history;

  @BeforeEach
  void setUp() {
    history = smsHistoryService.createHistory(1L, "01012345678", "테스트 SMS");

    em.flush();
    em.clear();
  }

  @Test
  void createHistory() {
    SmsSendHistory history = smsHistoryService.createHistory(1L, "01012345678", "테스트 SMS");

    assertThat(history.getId()).isNotNull();
  }

  @Test
  void markAsSuccess() {
    smsHistoryService.markAsSuccess(history.getId(), "success");
    em.flush();
    em.clear();

    SmsSendHistory found = smsHistoryQueryService.find(history.getId());

    assertThat(found.getSenderResponse()).isEqualTo("success");
    assertThat(found.getStatus()).isEqualTo(SmsSendStatus.SUCCESS);
  }

  @Test
  void markAsFailed() {
    smsHistoryService.markAsFailed(history.getId(), "failed");
    em.flush();
    em.clear();

    SmsSendHistory found = smsHistoryQueryService.find(history.getId());

    assertThat(found.getErrorMessage()).isEqualTo("failed");
    assertThat(found.getStatus()).isEqualTo(SmsSendStatus.FAILED);
  }
}
