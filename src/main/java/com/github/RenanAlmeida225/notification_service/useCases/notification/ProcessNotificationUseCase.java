package com.github.RenanAlmeida225.notification_service.useCases.notification;

import com.github.RenanAlmeida225.notification_service.infra.database.repositories.NotificationRepository;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;
import com.github.RenanAlmeida225.notification_service.useCases.sender.NotificationSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProcessNotificationUseCase {
    private static final int MAX_ATTEMPTS = 3;

    private final NotificationRepository repository;
    private final NotificationSender sender;

    public ProcessNotificationUseCase(NotificationRepository repository, NotificationSender sender) {
        this.repository = repository;
        this.sender = sender;
    }

    @Async
    public void execute(UUID notificationId) {

        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notification.getStatus() == NotificationStatus.SENT ||
                notification.getStatus() == NotificationStatus.FAILED) {
            return;
        }

        if (!notification.canRetryNow(MAX_ATTEMPTS)) {
            return;
        }

        try {
            notification.markAsProcessing();
            repository.save(notification);

            sender.send(notification);

            notification.markAsSent();
        } catch (Exception e) {
            if (notification.canRetry(MAX_ATTEMPTS)) {
                notification.markAsRetrying();
            } else {
                notification.markAsFailed();
            }
        } finally {
            repository.save(notification);
        }
    }


}
