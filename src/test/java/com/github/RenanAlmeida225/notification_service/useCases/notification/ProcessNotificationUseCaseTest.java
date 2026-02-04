package com.github.RenanAlmeida225.notification_service.useCases.notification;

import com.github.RenanAlmeida225.notification_service.infra.database.repositories.NotificationRepository;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationChannel;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;
import com.github.RenanAlmeida225.notification_service.useCases.sender.NotificationSender;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ProcessNotificationUseCaseTest {

    @Test
    void execute_whenSendSucceeds_marksAsSent() {
        InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
        FakeNotificationSender sender = new FakeNotificationSender(false);
        ProcessNotificationUseCase useCase = new ProcessNotificationUseCase(
                repository,
                sender,
                3,
                5,
                2
        );

        Notification notification = new Notification(
                NotificationChannel.EMAIL,
                "user@example.com",
                "Hello",
                "Test message"
        );
        repository.save(notification);

        useCase.execute(notification.getId());

        Notification saved = repository.findById(notification.getId()).orElseThrow();
        assertEquals(NotificationStatus.SENT, saved.getStatus());
        assertEquals(1, saved.getAttempts());
    }

    @Test
    void execute_whenSendFails_marksAsRetrying() {
        InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
        FakeNotificationSender sender = new FakeNotificationSender(true);
        ProcessNotificationUseCase useCase = new ProcessNotificationUseCase(
                repository,
                sender,
                3,
                5,
                2
        );

        Notification notification = new Notification(
                NotificationChannel.SMS,
                "000000000",
                "Hi",
                "Test message"
        );
        repository.save(notification);

        useCase.execute(notification.getId());

        Notification saved = repository.findById(notification.getId()).orElseThrow();
        assertEquals(NotificationStatus.RETRYING, saved.getStatus());
        assertEquals(1, saved.getAttempts());
        assertFalse(saved.canRetryNow(3));
    }

    @Test
    void execute_whenRetrying_canProcessAgain() {
        InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
        FakeNotificationSender sender = new FakeNotificationSender(false);
        ProcessNotificationUseCase useCase = new ProcessNotificationUseCase(
                repository,
                sender,
                3,
                0,
                2
        );

        Notification notification = new Notification(
                NotificationChannel.EMAIL,
                "user@example.com",
                "Hello",
                "Test message"
        );
        notification.markAsRetrying(0);
        repository.save(notification);

        useCase.execute(notification.getId());

        Notification saved = repository.findById(notification.getId()).orElseThrow();
        assertEquals(NotificationStatus.SENT, saved.getStatus());
        assertEquals(1, saved.getAttempts());
    }

    private static class FakeNotificationSender implements NotificationSender {
        private final boolean shouldFail;

        FakeNotificationSender(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public void send(Notification notification) {
            if (shouldFail) {
                throw new RuntimeException("Forced failure");
            }
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
    }
}
