package com.github.RenanAlmeida225.notification_service.api.controllers.dto;

import com.github.RenanAlmeida225.notification_service.models.notification.NotificationChannel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendNotificationRequest(
        @NotNull NotificationChannel channel,
        @NotBlank @Email String recipient,
        @NotBlank String title,
        @NotBlank String message
) {
}
