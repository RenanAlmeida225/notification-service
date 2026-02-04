package com.github.RenanAlmeida225.notification_service.infra.database.repositories;

import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    List<Notification> findAll();

    long count();

    long countByStatus(NotificationStatus status);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByStatusIn(List<NotificationStatus> statuses);

    Optional<Notification> findByIdempotencyKey(String idempotencyKey);


}
