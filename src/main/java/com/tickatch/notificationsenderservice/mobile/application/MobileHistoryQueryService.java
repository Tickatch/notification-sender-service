package com.tickatch.notificationsenderservice.mobile.application;

import com.tickatch.notificationsenderservice.mobile.domain.MobileSendHistory;
import com.tickatch.notificationsenderservice.mobile.domain.MobileSendHistoryRepository;
import com.tickatch.notificationsenderservice.mobile.domain.dto.MobileSendHistorySearchCondition;
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
public class MobileHistoryQueryService {

  private final MobileSendHistoryRepository mobileSendHistoryRepository;

  public MobileSendHistory find(Long historyId) {
    return mobileSendHistoryRepository
        .findById(historyId)
        .orElseThrow(() -> new IllegalArgumentException("SMS 발송 이력을 찾을 수 없습니다: " + historyId));
  }

  public Page<MobileSendHistory> search(
      MobileSendHistorySearchCondition condition, Pageable pageable) {
    LocalDateTime startDate =
        StringUtils.hasText(condition.startDate())
            ? LocalDateTime.parse(condition.startDate())
            : null;
    LocalDateTime endDate =
        StringUtils.hasText(condition.endDate()) ? LocalDateTime.parse(condition.endDate()) : null;

    return mobileSendHistoryRepository.search(
        condition.status(), condition.keyword(), startDate, endDate, pageable);
  }
}
