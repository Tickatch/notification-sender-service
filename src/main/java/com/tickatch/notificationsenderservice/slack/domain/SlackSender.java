package com.tickatch.notificationsenderservice.slack.domain;

import jakarta.validation.Valid;

public interface SlackSender {
  void sendDirectMessage(@Valid SlackDmSendRequest request);

  void sendChanelMessage(@Valid SlackChanelSendRequest request);
}
