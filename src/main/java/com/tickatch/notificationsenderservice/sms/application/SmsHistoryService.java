package com.tickatch.notificationsenderservice.sms.application;

import com.tickatch.notificationsenderservice.sms.domain.SmsSendHistory;
import com.tickatch.notificationsenderservice.sms.domain.SmsSendHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SmsHistoryService {

  private final SmsSendHistoryRepository smsSendHistoryRepository;

  private final SmsHistoryQueryService smsHistoryQueryService;

  public SmsSendHistory createHistory(Long notificationId, String phoneNumber, String content) {
    SmsSendHistory history = SmsSendHistory.create(notificationId, phoneNumber, content);

    return smsSendHistoryRepository.save(history);
  }

  public void markAsSuccess(Long historyId, String senderResponse) {
    SmsSendHistory history = smsHistoryQueryService.find(historyId);

    history.markAsSuccess(senderResponse);

    smsSendHistoryRepository.save(history);
  }

  public void markAsFailed(Long historyId, String errorMessage) {
    SmsSendHistory history = smsHistoryQueryService.find(historyId);

    history.markAsFailed(errorMessage);

    smsSendHistoryRepository.save(history);
  }
}
