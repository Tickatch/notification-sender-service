package com.tickatch.notificationsenderservice.slack.infrastructure.dto;

public class SlackMessageResponse extends AbstractSlackResponse {
  public SlackMessageResponse(boolean ok, String error) {
    super(ok, error);
  }
}
