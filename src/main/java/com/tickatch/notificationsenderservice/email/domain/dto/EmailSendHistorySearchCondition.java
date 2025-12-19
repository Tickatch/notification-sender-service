package com.tickatch.notificationsenderservice.email.domain.dto;

import com.tickatch.notificationsenderservice.email.domain.EmailSendStatus;

public record EmailSendHistorySearchCondition(
    EmailSendStatus status, String keyword, String startDate, String endDate) {}
