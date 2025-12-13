package com.tickatch.notificationsenderservice.slack.infrastructure.dto;

public record SlackMessageRequest(String channel, String text) {}
