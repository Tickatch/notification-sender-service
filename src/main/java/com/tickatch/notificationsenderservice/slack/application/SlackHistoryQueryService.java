package com.tickatch.notificationsenderservice.slack.application;

import com.tickatch.notificationsenderservice.slack.domain.SlackSendHistory;
import com.tickatch.notificationsenderservice.slack.domain.SlackSendHistoryRepository;
import com.tickatch.notificationsenderservice.slack.domain.dto.SlackSendHistorySearchCondition;
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
public class SlackHistoryQueryService {

  private final SlackSendHistoryRepository slackSendHistoryRepository;

  public SlackSendHistory find(Long historyId) {
    return slackSendHistoryRepository
        .findById(historyId)
        .orElseThrow(() -> new IllegalArgumentException("Slack 발송 이력을 찾을 수 없습니다: " + historyId));
  }

  public Page<SlackSendHistory> search(
      SlackSendHistorySearchCondition condition, Pageable pageable) {
    LocalDateTime startDate =
        StringUtils.hasText(condition.startDate())
            ? LocalDateTime.parse(condition.startDate())
            : null;
    LocalDateTime endDate =
        StringUtils.hasText(condition.endDate()) ? LocalDateTime.parse(condition.endDate()) : null;

    return slackSendHistoryRepository.search(
        condition.type(), condition.status(), condition.keyword(), startDate, endDate, pageable);
  }
}
