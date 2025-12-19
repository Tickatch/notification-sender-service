package com.tickatch.notificationsenderservice.mobile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.tickatch.notificationsenderservice.global.infrastructure.SendResultEvent;
import com.tickatch.notificationsenderservice.global.infrastructure.SendResultPublisher;
import com.tickatch.notificationsenderservice.mobile.domain.MobileSendHistory;
import com.tickatch.notificationsenderservice.mobile.domain.MobileSendStatus;
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
class MobileHistoryServiceTest {
  private final MobileHistoryService mobileHistoryService;

  private final MobileHistoryQueryService mobileHistoryQueryService;

  private final EntityManager em;

  @MockitoBean private final SendResultPublisher sendResultPublisher;

  MobileSendHistory history;

  @BeforeEach
  void setUp() {
    history = mobileHistoryService.createHistory(1L, "01012345678", "테스트 SMS");

    em.flush();
    em.clear();
  }

  @Test
  void createHistory() {
    MobileSendHistory history = mobileHistoryService.createHistory(1L, "01012345678", "테스트 SMS");

    assertThat(history.getId()).isNotNull();
  }

  @Test
  void markAsSuccess() {
    doNothing().when(sendResultPublisher).publish(any(SendResultEvent.class));

    mobileHistoryService.markAsSuccess(history.getId(), "success");
    em.flush();
    em.clear();

    MobileSendHistory found = mobileHistoryQueryService.find(history.getId());

    assertThat(found.getSenderResponse()).isEqualTo("success");
    assertThat(found.getStatus()).isEqualTo(MobileSendStatus.SUCCESS);
    verify(sendResultPublisher).publish(any(SendResultEvent.class));
  }

  @Test
  void markAsFailed() {
    doNothing().when(sendResultPublisher).publish(any(SendResultEvent.class));

    mobileHistoryService.markAsFailed(history.getId(), "failed");
    em.flush();
    em.clear();

    MobileSendHistory found = mobileHistoryQueryService.find(history.getId());

    assertThat(found.getErrorMessage()).isEqualTo("failed");
    assertThat(found.getStatus()).isEqualTo(MobileSendStatus.FAILED);
    verify(sendResultPublisher).publish(any(SendResultEvent.class));
  }
}
