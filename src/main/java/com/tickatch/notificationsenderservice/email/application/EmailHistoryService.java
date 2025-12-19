package com.tickatch.notificationsenderservice.email.application;

import com.tickatch.notificationsenderservice.email.domain.EmailSendHistory;
import com.tickatch.notificationsenderservice.email.domain.EmailSendHistoryRepository;
import com.tickatch.notificationsenderservice.global.infrastructure.SendResultEvent;
import com.tickatch.notificationsenderservice.global.infrastructure.SendResultPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailHistoryService {

  private final EmailSendHistoryRepository emailSendHistoryRepository;

  private final EmailHistoryQueryService emailHistoryQueryService;

  private final SendResultPublisher sendResultPublisher;

  public EmailSendHistory createHistory(
      Long notificationId, String emailAddress, String subject, String content, Boolean isHtml) {
    EmailSendHistory history =
        EmailSendHistory.create(notificationId, emailAddress, subject, content, isHtml);

    return emailSendHistoryRepository.save(history);
  }

  public void markAsSuccess(Long historyId) {
    EmailSendHistory history = emailHistoryQueryService.find(historyId);

    history.markAsSuccess();

    emailSendHistoryRepository.save(history);

    sendResultPublisher.publish(new SendResultEvent(history.getNotificationId(), true, null));
  }

  public void markAsFailed(Long historyId, String errorMessage) {
    EmailSendHistory history = emailHistoryQueryService.find(historyId);

    history.markAsFailed(errorMessage);

    emailSendHistoryRepository.save(history);

    sendResultPublisher.publish(
        new SendResultEvent(history.getNotificationId(), false, errorMessage));
  }
}
