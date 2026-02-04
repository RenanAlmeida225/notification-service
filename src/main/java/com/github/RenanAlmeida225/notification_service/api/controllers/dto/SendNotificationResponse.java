package com.github.RenanAlmeida225.notification_service.api.controllers.dto;

import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;

import java.util.UUID;

public record SendNotificationResponse(
        UUID id,
        NotificationStatus status
) {
}
