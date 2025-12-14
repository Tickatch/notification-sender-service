package com.tickatch.notificationsenderservice.slack.application;

import com.tickatch.notificationsenderservice.slack.domain.SlackSendHistory;
import com.tickatch.notificationsenderservice.slack.domain.SlackSendHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SlackHistoryService {

  private final SlackSendHistoryRepository smsSendHistoryRepository;

  private final SlackHistoryQueryService smsHistoryQueryService;

  public SlackSendHistory createDmHistory(String slackUserId, String content) {
    SlackSendHistory history = SlackSendHistory.createDm(slackUserId, content);

    return smsSendHistoryRepository.save(history);
  }

  public SlackSendHistory createChanelMessageHistory(String chanelId, String content) {
    SlackSendHistory history = SlackSendHistory.createChannel(chanelId, content);

    return smsSendHistoryRepository.save(history);
  }

  public void markAsSuccess(Long historyId, String smtpResponse) {
    SlackSendHistory history = smsHistoryQueryService.find(historyId);

    history.markAsSuccess(smtpResponse);

    smsSendHistoryRepository.save(history);
  }

  public void markAsFailed(Long historyId, String errorMessage) {
    SlackSendHistory history = smsHistoryQueryService.find(historyId);

    history.markAsFailed(errorMessage);

    smsSendHistoryRepository.save(history);
  }
}
