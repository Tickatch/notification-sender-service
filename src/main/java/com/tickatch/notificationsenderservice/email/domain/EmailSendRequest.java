package com.tickatch.notificationsenderservice.email.domain;

import jakarta.validation.constraints.NotBlank;

public record EmailSendRequest(
    @NotBlank(message = "수신자 이메일은 필수입니다.") String to,
    @NotBlank(message = "제목은 필수입니다.") String subject,
    @NotBlank(message = "내용은 필수입니다.") String content,
    boolean isHtml) {}
