package com.tickatch.notificationsenderservice.email.application;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import com.tickatch.notificationsenderservice.email.domain.EmailSendHistory;
import com.tickatch.notificationsenderservice.email.domain.EmailSendHistoryRepository;
import com.tickatch.notificationsenderservice.email.domain.EmailSendStatus;
import com.tickatch.notificationsenderservice.email.domain.dto.EmailSendHistorySearchCondition;
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
class EmailHistoryQueryServiceTest {

  private final EntityManager em;

  private final EmailHistoryQueryService emailHistoryQueryService;

  private final EmailSendHistoryRepository emailSendHistoryRepository;

  EmailSendHistory history1;
  EmailSendHistory history2;
  EmailSendHistory history3;
  EmailSendHistory history4;
  EmailSendHistory history5;
  EmailSendHistory history6;
  EmailSendHistory history7;
  EmailSendHistory history8;
  EmailSendHistory history9;
  EmailSendHistory history10;

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  @BeforeAll
  void setUpAll() {
    history1 = create("test1@example.com", "테스트 이메일", "테스트 이메일입니다.");
    history2 = create("test2@example.com", "테스트 이메일", "테스트 이메일입니다.");
    history3 = create("test3@naver.com", "테스트 이메일", "테스트 이메일입니다.");
    history4 = create("test4@naver.com", "테스트 이메일", "테스트 이메일입니다.");
    history5 = create("test5@example.com", "테스트 이메일", "테스트 이메일입니다.");
    history6 = create("test6@example.com", "테스트 이메일", "테스트 이메일입니다.");
    history7 = create("test7@example.com", "테스트 이메일", "테스트 이메일입니다.");
    history8 = create("test8@example.com", "테스트 이메일", "테스트 이메일입니다.");
    history9 = create("search9@example.com", "테스트 이메일", "테스트 이메일입니다.");
    history10 = create("search0@example.com", "테스트 이메일", "테스트 이메일입니다.");

    history5.markAsSuccess();
    history6.markAsSuccess();
    history7.markAsFailed("failed");
    history8.markAsFailed("failed");

    emailSendHistoryRepository.save(history5);
    emailSendHistoryRepository.save(history6);
    emailSendHistoryRepository.save(history7);
    emailSendHistoryRepository.save(history8);
  }

  private EmailSendHistory create(String email, String subject, String content) {
    EmailSendHistory history = EmailSendHistory.create(email, subject, content, false);

    return emailSendHistoryRepository.save(history);
  }

  @BeforeEach
  void setUp() {
    em.flush();
    em.clear();
  }

  @Test
  void find() {
    EmailSendHistory found = emailHistoryQueryService.find(history1.getId());

    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(history1.getId());
    assertThat(found.getEmailAddress()).isEqualTo("test1@example.com");
    assertThat(found.getStatus()).isEqualTo(EmailSendStatus.PENDING);
  }

  @Test
  void findIfNotFound() {
    assertThatThrownBy(() -> emailHistoryQueryService.find(999L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void searchByStatus() {
    EmailSendHistorySearchCondition condition =
        new EmailSendHistorySearchCondition(EmailSendStatus.PENDING, null, null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<EmailSendHistory> histories = emailHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent())
        .containsExactlyInAnyOrder(history1, history2, history3, history4, history9, history10);
  }

  @Test
  void searchByEmail() {
    EmailSendHistorySearchCondition condition =
        new EmailSendHistorySearchCondition(null, "naver", null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<EmailSendHistory> histories = emailHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).containsExactlyInAnyOrder(history3, history4);
  }

  @Test
  void searchByErrorMessage() {
    EmailSendHistorySearchCondition condition =
        new EmailSendHistorySearchCondition(null, "failed", null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<EmailSendHistory> histories = emailHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).containsExactlyInAnyOrder(history7, history8);
  }

  @Test
  void searchByStartDate() {
    LocalDateTime startDate = LocalDateTime.now().plusDays(1);

    EmailSendHistorySearchCondition condition =
        new EmailSendHistorySearchCondition(null, null, startDate.format(DATE_FORMATTER), null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<EmailSendHistory> histories = emailHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).isEmpty();
  }

  @Test
  void searchByEndDate() {
    LocalDateTime endDate = LocalDateTime.now().minusDays(1);

    EmailSendHistorySearchCondition condition =
        new EmailSendHistorySearchCondition(null, null, null, endDate.format(DATE_FORMATTER));
    Pageable pageable = PageRequest.of(0, 10);

    Page<EmailSendHistory> histories = emailHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent()).isEmpty();
  }

  @Test
  void searchByDateRange() {
    LocalDateTime startDate = LocalDateTime.now().minusDays(1);
    LocalDateTime endDate = LocalDateTime.now().plusDays(1);

    EmailSendHistorySearchCondition condition =
        new EmailSendHistorySearchCondition(
            null, null, startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER));
    Pageable pageable = PageRequest.of(0, 10);

    Page<EmailSendHistory> histories = emailHistoryQueryService.search(condition, pageable);

    assertThat(histories.getContent())
        .containsExactlyInAnyOrder(
            history1, history2, history3, history4, history5, history6, history7, history8,
            history9, history10);
  }
}
