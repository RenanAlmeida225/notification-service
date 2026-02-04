package com.github.RenanAlmeida225.notification_service.api.controllers.dto;

import com.github.RenanAlmeida225.notification_service.models.notification.NotificationChannel;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationChannel channel,
        NotificationStatus status,
        String recipient,
        String title,
        String message,
        int attempts,
        Instant lastAttemptAt,
        Instant nextAttemptAt,
        Instant createdAt
) {
}
