package com.tickatch.notificationsenderservice.slack.domain;

import jakarta.validation.constraints.NotBlank;

public record SlackDmSendRequest(
    @NotBlank(message = "slackId는 필수입니다.") String slackId,
    @NotBlank(message = "내용은 필수입니다.") String message) {}
