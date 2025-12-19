package com.tickatch.notificationsenderservice.mobile.domain.dto;

import com.tickatch.notificationsenderservice.mobile.domain.MobileSendStatus;

public record MobileSendHistorySearchCondition(
    MobileSendStatus status, String keyword, String startDate, String endDate) {}
