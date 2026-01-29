package com.github.RenanAlmeida225.notification_service.useCases.sender;

import com.github.RenanAlmeida225.notification_service.models.notification.Notification;

public interface NotificationSender {
    void send(Notification notification);
}
