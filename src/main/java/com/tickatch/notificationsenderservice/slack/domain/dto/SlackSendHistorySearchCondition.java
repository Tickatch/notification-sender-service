package com.tickatch.notificationsenderservice.slack.domain.dto;

import com.tickatch.notificationsenderservice.slack.domain.SlackMessageType;
import com.tickatch.notificationsenderservice.slack.domain.SlackSendStatus;

public record SlackSendHistorySearchCondition(
    SlackMessageType type,
    SlackSendStatus status,
    String keyword,
    String startDate,
    String endDate) {}
