package com.tickatch.notificationsenderservice.slack.domain;

import jakarta.validation.constraints.NotBlank;

public record SlackChanelSendRequest(
    @NotBlank(message = "chanelId는 필수입니다.") String chanelId,
    @NotBlank(message = "내용은 필수입니다.") String message) {}
