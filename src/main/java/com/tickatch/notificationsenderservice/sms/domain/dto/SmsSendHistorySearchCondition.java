package com.tickatch.notificationsenderservice.sms.domain.dto;

import com.tickatch.notificationsenderservice.sms.domain.SmsSendStatus;

public record SmsSendHistorySearchCondition(
    SmsSendStatus status, String keyword, String startDate, String endDate) {}
