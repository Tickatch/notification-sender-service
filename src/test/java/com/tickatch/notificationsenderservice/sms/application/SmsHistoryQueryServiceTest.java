package com.tickatch.notificationsenderservice.sms.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.tickatch.notificationsenderservice.sms.domain.SmsSendHistory;
import com.tickatch.notificationsenderservice.sms.domain.SmsSendHistoryRepository;
import com.tickatch.notificationsenderservice.sms.domain.SmsSendStatus;
import com.tickatch.notificationsenderservice.sms.domain.dto.SmsSendHistorySearchCondition;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RequiredArgsConstructor
class SmsHistoryQueryServiceTest {

  private final EntityManager em;

  private final SmsHistoryQueryService smsHistoryQueryService;

  private final SmsSendHistoryRepository smsSendHistoryRepository;

  SmsSendHistory history1;
  SmsSendHistory history2;
  SmsSendHistory history3;
  SmsSendHistory history4;
  SmsSendHistory history5;
  SmsSendHistory history6;
  SmsSendHistory history7;
  SmsSendHistory history8;
  SmsSendHistory history9;
  SmsSendHistory history10;

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  @BeforeAll
  void setUpAll() {
    history1 = create("01012345678", "테스트 SMS");
    history2 = create("01012345678", "테스트 SMS");
    history3 = create("01012345678", "테스트 SMS");
    history4 = create("01012345678", "테스트 SMS");
    history5 = create("01012345678", "테스트 SMS");
    history6 = create("01098765432", "테스트 SMS");
    history7 = create("01098765432", "테스트 SMS");
    history8 = create("01098765432", "테스트 SMS");
    history9 = create("01098765432", "테스트 SMS");
    history10 = create("01098765432", "테스트 SMS");

    history5.markAsSuccess("success");
    history6.markAsSuccess("success");
    history7.markAsFailed("failed");
    history8.markAsFailed("failed");

    smsSendHistoryRepository.save(history5);
    smsSendHistoryRepository.save(history6);
    smsSendHistoryRepository.save(history7);
    smsSendHistoryRepository.save(history8);
  }

  private SmsSendHistory create(String phoneNumber, String content) {
    SmsSendHistory history = SmsSendHistory.create(phoneNumber, content);

    return smsSendHistoryRepository.save(history);
  }

  @BeforeEach
  void setUp() {
    em.flush();
    em.clear();
  }

  @Test
  void find() {
    SmsSendHistory found = smsHistoryQueryService.find(history1.getId());

    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(history1.getId());
    assertThat(found.getPhoneNumber()).isEqualTo("01012345678");
    assertThat(found.getStatus()).isEqualTo(SmsSendStatus.PENDING);
  }

  @Test
  void findIfNotFound() {
    assertThatThrownBy(() -> smsHistoryQueryService.find(999L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void searchByStatus() {
    SmsSendHistorySearchCondition condition =
        new SmsSendHistorySearchCondition(SmsSendStatus.PENDING, null, null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<SmsSendHistory> histories = smsHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent())
        .containsExactlyInAnyOrder(history1, history2, history3, history4, history9, history10);
  }

  @Test
  void searchByPhoneNumber() {
    SmsSendHistorySearchCondition condition =
        new SmsSendHistorySearchCondition(null, "1234", null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<SmsSendHistory> histories = smsHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent())
        .containsExactlyInAnyOrder(history1, history2, history3, history4, history5);
  }

  @Test
  void searchBySenderResponse() {
    SmsSendHistorySearchCondition condition =
        new SmsSendHistorySearchCondition(null, "success", null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<SmsSendHistory> histories = smsHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).containsExactlyInAnyOrder(history5, history6);
  }

  @Test
  void searchByErrorMessage() {
    SmsSendHistorySearchCondition condition =
        new SmsSendHistorySearchCondition(null, "failed", null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<SmsSendHistory> histories = smsHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).containsExactlyInAnyOrder(history7, history8);
  }

  @Test
  void searchByStartDate() {
    LocalDateTime startDate = LocalDateTime.now().plusDays(1);

    SmsSendHistorySearchCondition condition =
        new SmsSendHistorySearchCondition(null, null, startDate.format(DATE_FORMATTER), null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<SmsSendHistory> histories = smsHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).isEmpty();
  }

  @Test
  void searchByEndDate() {
    LocalDateTime endDate = LocalDateTime.now().minusDays(1);

    SmsSendHistorySearchCondition condition =
        new SmsSendHistorySearchCondition(null, null, null, endDate.format(DATE_FORMATTER));
    Pageable pageable = PageRequest.of(0, 10);

    Page<SmsSendHistory> histories = smsHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).isEmpty();
  }

  @Test
  void searchByDateRange() {
    LocalDateTime startDate = LocalDateTime.now().minusDays(1);
    LocalDateTime endDate = LocalDateTime.now().plusDays(1);

    SmsSendHistorySearchCondition condition =
        new SmsSendHistorySearchCondition(
            null, null, startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER));
    Pageable pageable = PageRequest.of(0, 10);

    Page<SmsSendHistory> histories = smsHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent())
        .containsExactlyInAnyOrder(
            history1, history2, history3, history4, history5, history6, history7, history8,
            history9, history10);
  }
}
