package com.tickatch.notificationsenderservice.email.domain;

import com.tickatch.notificationsenderservice.email.domain.dto.EmailSendRequest;
import jakarta.validation.Valid;

public interface EmailSender {
  void send(@Valid EmailSendRequest request);
}
