package com.github.RenanAlmeida225.notification_service.useCases.notification;

import com.github.RenanAlmeida225.notification_service.infra.database.repositories.NotificationRepository;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationChannel;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetryNotificationsSchedulerTest {

    @Test
    void requeueEligibleRetries_publishesOnlyEligible() {
        InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
        RecordingPublisher publisher = new RecordingPublisher();
        RetryNotificationsScheduler scheduler = new RetryNotificationsScheduler(repository, publisher, 3);

        Notification eligible = new Notification(NotificationChannel.EMAIL, "a@example.com", "A", "A");
        eligible.markAsRetrying(0);
        repository.save(eligible);

        Notification notReady = new Notification(NotificationChannel.EMAIL, "b@example.com", "B", "B");
        notReady.markAsRetrying(60);
        repository.save(notReady);

        scheduler.requeueEligibleRetries();

        assertEquals(1, publisher.published.size());
        assertEquals(eligible.getId(), publisher.published.getFirst());
    }

    private static class RecordingPublisher extends PublishNotificationUseCase {
        private final List<UUID> published = new ArrayList<>();

        RecordingPublisher() {
            super(null);
        }

        @Override
        public void publish(UUID notificationId) {
            published.add(notificationId);
        }
    }

    private static class InMemoryNotificationRepository implements NotificationRepository {
        private final Map<UUID, Notification> store = new HashMap<>();

        @Override
        public Notification save(Notification notification) {
            store.put(notification.getId(), notification);
            return notification;
        }

        @Override
        public Optional<Notification> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Notification> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public long countByStatus(NotificationStatus status) {
            return store.values().stream()
                    .filter(notification -> notification.getStatus() == status)
                    .count();
        }

        @Override
        public List<Notification> findByStatus(NotificationStatus status) {
            return store.values().stream()
                    .filter(notification -> notification.getStatus() == status)
                    .toList();
        }

        @Override
        public List<Notification> findByStatusIn(List<NotificationStatus> statuses) {
            return store.values().stream()
                    .filter(notification -> statuses.contains(notification.getStatus()))
                    .toList();
        }

        @Override
        public Optional<Notification> findByIdempotencyKey(String idempotencyKey) {
            return store.values().stream()
                    .filter(notification -> idempotencyKey.equals(notification.getIdempotencyKey()))
                    .findFirst();
        }
    }
}
