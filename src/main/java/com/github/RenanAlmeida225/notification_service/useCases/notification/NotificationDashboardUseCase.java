package com.github.RenanAlmeida225.notification_service.useCases.notification;

import com.github.RenanAlmeida225.notification_service.api.controllers.dto.NotificationDashboardResponse;
import com.github.RenanAlmeida225.notification_service.infra.database.repositories.NotificationRepository;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;
import org.springframework.stereotype.Service;

@Service
public class NotificationDashboardUseCase {
    private final NotificationRepository repository;

    public NotificationDashboardUseCase(NotificationRepository repository) {
        this.repository = repository;
    }

    public NotificationDashboardResponse execute() {
        long total = repository.count();
        long pending = repository.countByStatus(NotificationStatus.PENDING);
        long processing = repository.countByStatus(NotificationStatus.PROCESSING);
        long retrying = repository.countByStatus(NotificationStatus.RETRYING);
        long sent = repository.countByStatus(NotificationStatus.SENT);
        long failed = repository.countByStatus(NotificationStatus.FAILED);

        return new NotificationDashboardResponse(
                total,
                pending,
                processing,
                retrying,
                sent,
                failed
        );
    }
}
