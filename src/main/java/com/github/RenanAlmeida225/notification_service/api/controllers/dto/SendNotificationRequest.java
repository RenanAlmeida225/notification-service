package com.github.RenanAlmeida225.notification_service.api.controllers.dto;

import com.github.RenanAlmeida225.notification_service.models.notification.NotificationChannel;

public record SendNotificationRequest(
        NotificationChannel channel,
        String recipient,
        String title,
        String message
) {
}
