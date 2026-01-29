package com.github.RenanAlmeida225.notification_service.infra.database.repositories;

import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaNotificationRepository extends JpaRepository<Notification, UUID>,
        NotificationRepository {
    long countByStatus(NotificationStatus status);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByStatusIn(List<NotificationStatus> statuses);


}
