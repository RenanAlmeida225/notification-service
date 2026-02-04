package com.github.RenanAlmeida225.notification_service.useCases.notification;

import com.github.RenanAlmeida225.notification_service.infra.database.repositories.NotificationRepository;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationChannel;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SendNotificationUseCaseTest {

    @Test
    void execute_withIdempotencyKey_returnsSameId() {
        InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
        PublishNotificationUseCase publisher = new PublishNotificationUseCase(null) {
            @Override
            public void publish(UUID notificationId) {
                // no-op for tests
            }
        };
        SendNotificationUseCase useCase = new SendNotificationUseCase(repository, publisher);

        String key = "key-123";
        Notification first = new Notification(
                NotificationChannel.EMAIL,
                "user@example.com",
                "Hello",
                "Test message"
        );
        UUID firstId = useCase.execute(first, key);

        Notification second = new Notification(
                NotificationChannel.EMAIL,
                "user@example.com",
                "Hello",
                "Test message"
        );
        UUID secondId = useCase.execute(second, key);

        assertEquals(firstId, secondId);
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
