package com.tickatch.notificationsenderservice.email.domain;

import jakarta.validation.Valid;

public interface EmailSender {
  void send(@Valid EmailSendRequest request);
}
