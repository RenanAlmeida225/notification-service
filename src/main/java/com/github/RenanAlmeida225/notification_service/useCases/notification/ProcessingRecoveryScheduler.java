package com.github.RenanAlmeida225.notification_service.useCases.notification;

import com.github.RenanAlmeida225.notification_service.infra.database.repositories.NotificationRepository;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ProcessingRecoveryScheduler {
    private final NotificationRepository repository;
    private final int maxAttempts;
    private final long processingTimeoutSeconds;

    public ProcessingRecoveryScheduler(
            NotificationRepository repository,
            @Value("${notification.retry.max-attempts:3}") int maxAttempts,
            @Value("${notification.processing.timeout-seconds:120}") long processingTimeoutSeconds
    ) {
        this.repository = repository;
        this.maxAttempts = maxAttempts;
        this.processingTimeoutSeconds = processingTimeoutSeconds;
    }

    @Scheduled(fixedDelayString = "${notification.processing.recovery-delay-ms:10000}")
    public void recoverStuckProcessing() {
        List<Notification> processing = repository.findByStatus(NotificationStatus.PROCESSING);
        Instant cutoff = Instant.now().minusSeconds(processingTimeoutSeconds);

        for (Notification notification : processing) {
            if (notification.getLastAttemptAt() == null) {
                continue;
            }
            if (notification.getLastAttemptAt().isAfter(cutoff)) {
                continue;
            }

            if (notification.canRetry(maxAttempts)) {
                notification.markAsRetrying(0);
            } else {
                notification.markAsFailed();
            }
            repository.save(notification);
        }
    }
}
