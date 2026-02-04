package com.github.RenanAlmeida225.notification_service.useCases.notification;

import com.github.RenanAlmeida225.notification_service.infra.database.repositories.NotificationRepository;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RetryNotificationsScheduler {
    private final NotificationRepository repository;
    private final PublishNotificationUseCase publisher;
    private final int maxAttempts;

    public RetryNotificationsScheduler(
            NotificationRepository repository,
            PublishNotificationUseCase publisher,
            @Value("${notification.retry.max-attempts:3}") int maxAttempts
    ) {
        this.repository = repository;
        this.publisher = publisher;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${notification.retry.scheduler-delay-ms:5000}")
    public void requeueEligibleRetries() {
        List<Notification> retrying = repository.findByStatus(NotificationStatus.RETRYING);

        for (Notification notification : retrying) {
            if (notification.canRetryNow(maxAttempts)) {
                publisher.publish(notification.getId());
            }
        }
    }
}
