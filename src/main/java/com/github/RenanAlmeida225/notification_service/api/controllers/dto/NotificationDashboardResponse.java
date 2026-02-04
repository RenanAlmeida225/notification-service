package com.github.RenanAlmeida225.notification_service.api.controllers.dto;

public record NotificationDashboardResponse(
        long total,
        long pending,
        long processing,
        long retrying,
        long sent,
        long failed
) {
}
