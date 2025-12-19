package com.tickatch.notificationsenderservice.mobile.domain;

import com.tickatch.notificationsenderservice.mobile.domain.dto.MmsSendRequest;
import com.tickatch.notificationsenderservice.mobile.domain.dto.SmsSendRequest;
import jakarta.validation.Valid;

public interface MobileSender {
  String send(@Valid SmsSendRequest request);

  String send(@Valid MmsSendRequest request);
}
