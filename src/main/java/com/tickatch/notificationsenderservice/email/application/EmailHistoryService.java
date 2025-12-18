package com.tickatch.notificationsenderservice.email.application;

import com.tickatch.notificationsenderservice.email.domain.EmailSendHistory;
import com.tickatch.notificationsenderservice.email.domain.EmailSendHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailHistoryService {

  private final EmailSendHistoryRepository emailSendHistoryRepository;

  private final EmailHistoryQueryService emailHistoryQueryService;

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
  }

  public void markAsFailed(Long historyId, String errorMessage) {
    EmailSendHistory history = emailHistoryQueryService.find(historyId);

    history.markAsFailed(errorMessage);

    emailSendHistoryRepository.save(history);
  }
}
