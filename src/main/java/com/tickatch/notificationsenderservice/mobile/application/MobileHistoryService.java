package com.tickatch.notificationsenderservice.mobile.application;

import com.tickatch.notificationsenderservice.global.infrastructure.SendResultEvent;
import com.tickatch.notificationsenderservice.global.infrastructure.SendResultPublisher;
import com.tickatch.notificationsenderservice.mobile.domain.MobileSendHistory;
import com.tickatch.notificationsenderservice.mobile.domain.MobileSendHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MobileHistoryService {

  private final MobileSendHistoryRepository mobileSendHistoryRepository;

  private final MobileHistoryQueryService mobileHistoryQueryService;

  private final SendResultPublisher sendResultPublisher;

  public MobileSendHistory createHistory(Long notificationId, String phoneNumber, String content) {
    MobileSendHistory history = MobileSendHistory.create(notificationId, phoneNumber, content);

    return mobileSendHistoryRepository.save(history);
  }

  public void markAsSuccess(Long historyId, String senderResponse) {
    MobileSendHistory history = mobileHistoryQueryService.find(historyId);

    history.markAsSuccess(senderResponse);

    mobileSendHistoryRepository.save(history);

    sendResultPublisher.publish(new SendResultEvent(history.getNotificationId(), true, null));
  }

  public void markAsFailed(Long historyId, String errorMessage) {
    MobileSendHistory history = mobileHistoryQueryService.find(historyId);

    history.markAsFailed(errorMessage);

    mobileSendHistoryRepository.save(history);

    sendResultPublisher.publish(
        new SendResultEvent(history.getNotificationId(), false, errorMessage));
  }
}
