package com.tickatch.notificationsenderservice.mobile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.tickatch.notificationsenderservice.mobile.domain.MobileSendHistory;
import com.tickatch.notificationsenderservice.mobile.domain.MobileSendHistoryRepository;
import com.tickatch.notificationsenderservice.mobile.domain.MobileSendStatus;
import com.tickatch.notificationsenderservice.mobile.domain.dto.MobileSendHistorySearchCondition;
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
class MobileHistoryQueryServiceTest {

  private final EntityManager em;

  private final MobileHistoryQueryService mobileHistoryQueryService;

  private final MobileSendHistoryRepository mobileSendHistoryRepository;

  MobileSendHistory history1;
  MobileSendHistory history2;
  MobileSendHistory history3;
  MobileSendHistory history4;
  MobileSendHistory history5;
  MobileSendHistory history6;
  MobileSendHistory history7;
  MobileSendHistory history8;
  MobileSendHistory history9;
  MobileSendHistory history10;

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  @BeforeAll
  void setUpAll() {
    history1 = create(1L, "01012345678", "테스트 SMS");
    history2 = create(2L, "01012345678", "테스트 SMS");
    history3 = create(3L, "01012345678", "테스트 SMS");
    history4 = create(4L, "01012345678", "테스트 SMS");
    history5 = create(5L, "01012345678", "테스트 SMS");
    history6 = create(6L, "01098765432", "테스트 SMS");
    history7 = create(7L, "01098765432", "테스트 SMS");
    history8 = create(8L, "01098765432", "테스트 SMS");
    history9 = create(9L, "01098765432", "테스트 SMS");
    history10 = create(10L, "01098765432", "테스트 SMS");

    history5.markAsSuccess("success");
    history6.markAsSuccess("success");
    history7.markAsFailed("failed");
    history8.markAsFailed("failed");

    mobileSendHistoryRepository.save(history5);
    mobileSendHistoryRepository.save(history6);
    mobileSendHistoryRepository.save(history7);
    mobileSendHistoryRepository.save(history8);
  }

  private MobileSendHistory create(Long notificationId, String phoneNumber, String content) {
    MobileSendHistory history = MobileSendHistory.create(notificationId, phoneNumber, content);

    return mobileSendHistoryRepository.save(history);
  }

  @BeforeEach
  void setUp() {
    em.flush();
    em.clear();
  }

  @Test
  void find() {
    MobileSendHistory found = mobileHistoryQueryService.find(history1.getId());

    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(history1.getId());
    assertThat(found.getPhoneNumber()).isEqualTo("01012345678");
    assertThat(found.getStatus()).isEqualTo(MobileSendStatus.PENDING);
  }

  @Test
  void findIfNotFound() {
    assertThatThrownBy(() -> mobileHistoryQueryService.find(999L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void searchByStatus() {
    MobileSendHistorySearchCondition condition =
        new MobileSendHistorySearchCondition(MobileSendStatus.PENDING, null, null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<MobileSendHistory> histories = mobileHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent())
        .containsExactlyInAnyOrder(history1, history2, history3, history4, history9, history10);
  }

  @Test
  void searchByPhoneNumber() {
    MobileSendHistorySearchCondition condition =
        new MobileSendHistorySearchCondition(null, "1234", null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<MobileSendHistory> histories = mobileHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent())
        .containsExactlyInAnyOrder(history1, history2, history3, history4, history5);
  }

  @Test
  void searchBySenderResponse() {
    MobileSendHistorySearchCondition condition =
        new MobileSendHistorySearchCondition(null, "success", null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<MobileSendHistory> histories = mobileHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).containsExactlyInAnyOrder(history5, history6);
  }

  @Test
  void searchByErrorMessage() {
    MobileSendHistorySearchCondition condition =
        new MobileSendHistorySearchCondition(null, "failed", null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<MobileSendHistory> histories = mobileHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).containsExactlyInAnyOrder(history7, history8);
  }

  @Test
  void searchByStartDate() {
    LocalDateTime startDate = LocalDateTime.now().plusDays(1);

    MobileSendHistorySearchCondition condition =
        new MobileSendHistorySearchCondition(null, null, startDate.format(DATE_FORMATTER), null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<MobileSendHistory> histories = mobileHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).isEmpty();
  }

  @Test
  void searchByEndDate() {
    LocalDateTime endDate = LocalDateTime.now().minusDays(1);

    MobileSendHistorySearchCondition condition =
        new MobileSendHistorySearchCondition(null, null, null, endDate.format(DATE_FORMATTER));
    Pageable pageable = PageRequest.of(0, 10);

    Page<MobileSendHistory> histories = mobileHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).isEmpty();
  }

  @Test
  void searchByDateRange() {
    LocalDateTime startDate = LocalDateTime.now().minusDays(1);
    LocalDateTime endDate = LocalDateTime.now().plusDays(1);

    MobileSendHistorySearchCondition condition =
        new MobileSendHistorySearchCondition(
            null, null, startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER));
    Pageable pageable = PageRequest.of(0, 10);

    Page<MobileSendHistory> histories = mobileHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent())
        .containsExactlyInAnyOrder(
            history1, history2, history3, history4, history5, history6, history7, history8,
            history9, history10);
  }
}
