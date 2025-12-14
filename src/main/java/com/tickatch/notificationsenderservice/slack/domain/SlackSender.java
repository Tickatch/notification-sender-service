package com.tickatch.notificationsenderservice.slack.domain;

import com.tickatch.notificationsenderservice.slack.domain.dto.SlackChanelSendRequest;
import com.tickatch.notificationsenderservice.slack.domain.dto.SlackDmSendRequest;
import jakarta.validation.Valid;

public interface SlackSender {
  void sendDirectMessage(@Valid SlackDmSendRequest request);

  void sendChanelMessage(@Valid SlackChanelSendRequest request);
}
