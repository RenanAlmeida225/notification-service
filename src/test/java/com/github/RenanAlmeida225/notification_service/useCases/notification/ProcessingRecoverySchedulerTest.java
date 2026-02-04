package com.github.RenanAlmeida225.notification_service.useCases.notification;

import com.github.RenanAlmeida225.notification_service.infra.database.repositories.NotificationRepository;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationChannel;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessingRecoverySchedulerTest {

    @Test
    void recoverStuckProcessing_marksAsRetryingWhenAttemptsRemain() throws Exception {
        InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
        ProcessingRecoveryScheduler scheduler = new ProcessingRecoveryScheduler(repository, 3, 60);

        Notification notification = new Notification(NotificationChannel.EMAIL, "a@example.com", "A", "A");
        notification.markAsProcessing();
        setLastAttemptAt(notification, Instant.now().minusSeconds(120));
        repository.save(notification);

        scheduler.recoverStuckProcessing();

        Notification updated = repository.findById(notification.getId()).orElseThrow();
        assertEquals(NotificationStatus.RETRYING, updated.getStatus());
    }

    @Test
    void recoverStuckProcessing_marksAsFailedWhenMaxAttemptsReached() throws Exception {
        InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
        ProcessingRecoveryScheduler scheduler = new ProcessingRecoveryScheduler(repository, 2, 60);

        Notification notification = new Notification(NotificationChannel.EMAIL, "b@example.com", "B", "B");
        notification.markAsProcessing();
        notification.markAsRetrying(0);
        notification.markAsProcessing();
        setLastAttemptAt(notification, Instant.now().minusSeconds(120));
        repository.save(notification);

        scheduler.recoverStuckProcessing();

        Notification updated = repository.findById(notification.getId()).orElseThrow();
        assertEquals(NotificationStatus.FAILED, updated.getStatus());
    }

    private static void setLastAttemptAt(Notification notification, Instant value) throws Exception {
        Field field = Notification.class.getDeclaredField("lastAttemptAt");
        field.setAccessible(true);
        field.set(notification, value);
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
