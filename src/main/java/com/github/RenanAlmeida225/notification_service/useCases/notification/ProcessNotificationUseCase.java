package com.github.RenanAlmeida225.notification_service.useCases.notification;

import com.github.RenanAlmeida225.notification_service.infra.database.repositories.NotificationRepository;
import com.github.RenanAlmeida225.notification_service.infra.metrics.NotificationMetrics;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;
import com.github.RenanAlmeida225.notification_service.useCases.sender.NotificationSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Service
public class ProcessNotificationUseCase {
    private static final Logger logger = LoggerFactory.getLogger(ProcessNotificationUseCase.class);
    private final NotificationRepository repository;
    private final NotificationSender sender;
    private final NotificationMetrics metrics;
    private final int maxAttempts;
    private final long initialBackoffSeconds;
    private final int backoffMultiplier;

    public ProcessNotificationUseCase(
            NotificationRepository repository,
            NotificationSender sender,
            NotificationMetrics metrics,
            @Value("${notification.retry.max-attempts:3}") int maxAttempts,
            @Value("${notification.retry.initial-backoff-seconds:5}") long initialBackoffSeconds,
            @Value("${notification.retry.backoff-multiplier:2}") int backoffMultiplier
    ) {
        this.repository = repository;
        this.sender = sender;
        this.metrics = metrics;
        this.maxAttempts = maxAttempts;
        this.initialBackoffSeconds = initialBackoffSeconds;
        this.backoffMultiplier = backoffMultiplier;
    }

    @Async
    public void execute(UUID notificationId) {

        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notification.getStatus() == NotificationStatus.SENT ||
                notification.getStatus() == NotificationStatus.FAILED) {
            return;
        }

        if (!notification.canRetryNow(maxAttempts)) {
            return;
        }

        try {
            notification.markAsProcessing();
            notification = repository.save(notification);

            sender.send(notification);

            notification.markAsSent();
            metrics.incrementSent();
        } catch (Exception e) {
            logger.error("Failed to send notification {}. Will mark as retrying/failed.", notification.getId(), e);
            if (notification.canRetry(maxAttempts)) {
                notification.markAsRetrying(calculateBackoffSeconds(notification.getAttempts()));
                metrics.incrementRetrying();
            } else {
                notification.markAsFailed();
                metrics.incrementFailed();
            }
        } finally {
            repository.save(notification);
        }
    }

    private long calculateBackoffSeconds(int attempts) {
        if (attempts <= 0) {
            return initialBackoffSeconds;
        }
        double exponent = Math.max(0, attempts - 1);
        double backoff = initialBackoffSeconds * Math.pow(backoffMultiplier, exponent);
        return Math.max(1L, Math.round(backoff));
    }

}
