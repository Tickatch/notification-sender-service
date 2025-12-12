package com.tickatch.notificationsenderservice.sms.domain;

import jakarta.validation.Valid;

public interface SmsSender {
  void send(@Valid SmsSendRequest request);
}
