package com.github.RenanAlmeida225.notification_service.useCases.notification;

import com.github.RenanAlmeida225.notification_service.infra.database.repositories.NotificationRepository;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
        return execute(notification, null);
    }

    @Transactional
    public UUID execute(Notification notification, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            notification.setIdempotencyKey(idempotencyKey);
            return repository.findByIdempotencyKey(idempotencyKey)
                    .map(Notification::getId)
                    .orElseGet(() -> saveAndPublish(notification));
        }

        return saveAndPublish(notification);
    }

    private UUID saveAndPublish(Notification notification) {
        try {
            repository.save(notification);
        } catch (DataIntegrityViolationException e) {
            String key = notification.getIdempotencyKey();
            if (key != null && !key.isBlank()) {
                return repository.findByIdempotencyKey(key)
                        .map(Notification::getId)
                        .orElseThrow(() -> e);
            }
            throw e;
        }

        publishAfterCommit(notification.getId());
        return notification.getId();
    }

    private void publishAfterCommit(UUID id) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publisher.publish(id);
                }
            });
        } else {
            publisher.publish(id);
        }
    }

    public Optional<Notification> findById(UUID id) {
        return repository.findById(id);
    }

    public List<Notification> findAll() {
        return repository.findAll();
    }
}
