package com.tickatch.notificationsenderservice.sms.application;

import com.tickatch.notificationsenderservice.sms.domain.SmsSendHistory;
import com.tickatch.notificationsenderservice.sms.domain.SmsSendHistoryRepository;
import com.tickatch.notificationsenderservice.sms.domain.dto.SmsSendHistorySearchCondition;
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
public class SmsHistoryQueryService {

  private final SmsSendHistoryRepository smsSendHistoryRepository;

  public SmsSendHistory find(Long historyId) {
    return smsSendHistoryRepository
        .findById(historyId)
        .orElseThrow(() -> new IllegalArgumentException("SMS 발송 이력을 찾을 수 없습니다: " + historyId));
  }

  public Page<SmsSendHistory> search(SmsSendHistorySearchCondition condition, Pageable pageable) {
    LocalDateTime startDate =
        StringUtils.hasText(condition.startDate())
            ? LocalDateTime.parse(condition.startDate())
            : null;
    LocalDateTime endDate =
        StringUtils.hasText(condition.endDate()) ? LocalDateTime.parse(condition.endDate()) : null;

    return smsSendHistoryRepository.search(
        condition.status(), condition.keyword(), startDate, endDate, pageable);
  }
}
