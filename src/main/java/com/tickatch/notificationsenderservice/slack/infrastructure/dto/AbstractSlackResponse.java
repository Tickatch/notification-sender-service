package com.tickatch.notificationsenderservice.slack.infrastructure.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AbstractSlackResponse {
  private final boolean ok;
  private final String error;
}
