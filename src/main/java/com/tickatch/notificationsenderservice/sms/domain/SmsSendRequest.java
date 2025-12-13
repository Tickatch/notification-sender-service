package com.tickatch.notificationsenderservice.sms.domain;

import jakarta.validation.constraints.NotBlank;

public record SmsSendRequest(
    @NotBlank(message = "수신자 번호는 필수입니다.") String to,
    @NotBlank(message = "내용은 필수입니다.") String content) {}
