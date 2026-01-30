package com.github.RenanAlmeida225.notification_service.useCases.notification;

import com.github.RenanAlmeida225.notification_service.infra.database.repositories.NotificationRepository;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SendNotificationUseCase {
    private final NotificationRepository repository;
    private final PublishNotificationUseCase publisher;

    public SendNotificationUseCase(NotificationRepository repository, PublishNotificationUseCase publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional
    public UUID execute(Notification notification) {
        repository.save(notification);
        publisher.publish(notification.getId());
        return notification.getId();
    }

    public Optional<Notification> findById(UUID id) {
        return repository.findById(id);
    }

    public List<Notification> findAll() {
        return repository.findAll();
    }
}
