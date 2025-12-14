package com.tickatch.notificationsenderservice.slack.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.tickatch.notificationsenderservice.slack.domain.SlackMessageType;
import com.tickatch.notificationsenderservice.slack.domain.SlackSendHistory;
import com.tickatch.notificationsenderservice.slack.domain.SlackSendHistoryRepository;
import com.tickatch.notificationsenderservice.slack.domain.SlackSendStatus;
import com.tickatch.notificationsenderservice.slack.domain.dto.SlackSendHistorySearchCondition;
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
class SlackHistoryQueryServiceTest {

  private final EntityManager em;

  private final SlackHistoryQueryService slackHistoryQueryService;

  private final SlackSendHistoryRepository slackSendHistoryRepository;

  SlackSendHistory history1;
  SlackSendHistory history2;
  SlackSendHistory history3;
  SlackSendHistory history4;
  SlackSendHistory history5;
  SlackSendHistory history6;
  SlackSendHistory history7;
  SlackSendHistory history8;
  SlackSendHistory history9;
  SlackSendHistory history10;

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  @BeforeAll
  void setUpAll() {
    history1 = createDm("U12345678", "테스트 메시지");
    history2 = createDm("U12345678", "테스트 메시지");
    history3 = createDm("U12345678", "테스트 메시지");
    history4 = createDm("U12345678", "테스트 메시지");
    history5 = createDm("U12345678", "테스트 메시지");
    history6 = createCm("C12345678", "테스트 메시지");
    history7 = createCm("C12345678", "테스트 메시지");
    history8 = createCm("C12345678", "테스트 메시지");
    history9 = createCm("C12345678", "테스트 메시지");
    history10 = createCm("C12345678", "테스트 메시지");

    history5.markAsSuccess("success");
    history6.markAsSuccess("success");
    history7.markAsFailed("failed");
    history8.markAsFailed("failed");

    slackSendHistoryRepository.save(history5);
    slackSendHistoryRepository.save(history6);
    slackSendHistoryRepository.save(history7);
    slackSendHistoryRepository.save(history8);
  }

  private SlackSendHistory createDm(String slackUserId, String content) {
    SlackSendHistory history = SlackSendHistory.createDm(slackUserId, content);

    return slackSendHistoryRepository.save(history);
  }

  private SlackSendHistory createCm(String channel, String content) {
    SlackSendHistory history = SlackSendHistory.createChannel(channel, content);

    return slackSendHistoryRepository.save(history);
  }

  @BeforeEach
  void setUp() {
    em.flush();
    em.clear();
  }

  @Test
  void find() {
    SlackSendHistory found = slackHistoryQueryService.find(history1.getId());

    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(history1.getId());
    assertThat(found.getStatus()).isEqualTo(SlackSendStatus.PENDING);
  }

  @Test
  void findIfNotFound() {
    assertThatThrownBy(() -> slackHistoryQueryService.find(999L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void searchByType() {
    SlackSendHistorySearchCondition condition =
        new SlackSendHistorySearchCondition(SlackMessageType.DM, null, null, null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<SlackSendHistory> histories = slackHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent())
        .containsExactlyInAnyOrder(history1, history2, history3, history4, history5);
  }

  @Test
  void searchByStatus() {
    SlackSendHistorySearchCondition condition =
        new SlackSendHistorySearchCondition(null, SlackSendStatus.PENDING, null, null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<SlackSendHistory> histories = slackHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent())
        .containsExactlyInAnyOrder(history1, history2, history3, history4, history9, history10);
  }

  @Test
  void searchBySlackUserId() {
    SlackSendHistorySearchCondition condition =
        new SlackSendHistorySearchCondition(null, null, "U1234", null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<SlackSendHistory> histories = slackHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent())
        .containsExactlyInAnyOrder(history1, history2, history3, history4, history5);
  }

  @Test
  void searchByChannelId() {
    SlackSendHistorySearchCondition condition =
        new SlackSendHistorySearchCondition(null, null, "C1234", null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<SlackSendHistory> histories = slackHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent())
        .containsExactlyInAnyOrder(history6, history7, history8, history9, history10);
  }

  @Test
  void searchBySenderResponse() {
    SlackSendHistorySearchCondition condition =
        new SlackSendHistorySearchCondition(null, null, "success", null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<SlackSendHistory> histories = slackHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).containsExactlyInAnyOrder(history5, history6);
  }

  @Test
  void searchByErrorMessage() {
    SlackSendHistorySearchCondition condition =
        new SlackSendHistorySearchCondition(null, null, "failed", null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<SlackSendHistory> histories = slackHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).containsExactlyInAnyOrder(history7, history8);
  }

  @Test
  void searchByStartDate() {
    LocalDateTime startDate = LocalDateTime.now().plusDays(1);

    SlackSendHistorySearchCondition condition =
        new SlackSendHistorySearchCondition(
            null, null, null, startDate.format(DATE_FORMATTER), null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<SlackSendHistory> histories = slackHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).isEmpty();
  }

  @Test
  void searchByEndDate() {
    LocalDateTime endDate = LocalDateTime.now().minusDays(1);

    SlackSendHistorySearchCondition condition =
        new SlackSendHistorySearchCondition(null, null, null, null, endDate.format(DATE_FORMATTER));
    Pageable pageable = PageRequest.of(0, 10);

    Page<SlackSendHistory> histories = slackHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).isEmpty();
  }

  @Test
  void searchByDateRange() {
    LocalDateTime startDate = LocalDateTime.now().minusDays(1);
    LocalDateTime endDate = LocalDateTime.now().plusDays(1);

    SlackSendHistorySearchCondition condition =
        new SlackSendHistorySearchCondition(
            null, null, null, startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER));
    Pageable pageable = PageRequest.of(0, 10);

    Page<SlackSendHistory> histories = slackHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent())
        .containsExactlyInAnyOrder(
            history1, history2, history3, history4, history5, history6, history7, history8,
            history9, history10);
  }
}
