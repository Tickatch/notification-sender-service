package com.tickatch.notificationsenderservice.sms.domain;

import com.tickatch.notificationsenderservice.sms.domain.dto.SmsSendRequest;
import jakarta.validation.Valid;

public interface SmsSender {
  String send(@Valid SmsSendRequest request);
}
