package com.tickatch.notificationsenderservice.email.application;

import com.tickatch.notificationsenderservice.email.domain.EmailSendHistory;
import com.tickatch.notificationsenderservice.email.domain.EmailSendHistoryRepository;
import com.tickatch.notificationsenderservice.email.domain.dto.EmailSendHistorySearchCondition;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailHistoryQueryService {

  private final EmailSendHistoryRepository emailHistoryRepository;

  public EmailSendHistory find(Long historyId) {
    return emailHistoryRepository
        .findById(historyId)
        .orElseThrow(() -> new IllegalArgumentException("이메일 발송 이력을 찾을 수 없습니다: " + historyId));
  }

  public Page<EmailSendHistory> search(
      EmailSendHistorySearchCondition condition, Pageable pageable) {
    LocalDateTime startDate =
        StringUtils.hasText(condition.startDate())
            ? LocalDateTime.parse(condition.startDate())
            : null;
    LocalDateTime endDate =
        StringUtils.hasText(condition.endDate()) ? LocalDateTime.parse(condition.endDate()) : null;

    return emailHistoryRepository.search(
        condition.status(), condition.keyword(), startDate, endDate, pageable);
  }
}
