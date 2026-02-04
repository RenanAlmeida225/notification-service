package com.github.RenanAlmeida225.notification_service.useCases.notification;

import com.github.RenanAlmeida225.notification_service.api.controllers.dto.NotificationDashboardResponse;
import com.github.RenanAlmeida225.notification_service.infra.database.repositories.NotificationRepository;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationChannel;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationDashboardUseCaseTest {

    @Test
    void execute_aggregatesCountsByStatus() {
        InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
        NotificationDashboardUseCase useCase = new NotificationDashboardUseCase(repository);

        Notification pending = new Notification(NotificationChannel.EMAIL, "a@example.com", "A", "A");
        Notification processing = new Notification(NotificationChannel.EMAIL, "b@example.com", "B", "B");
        processing.markAsProcessing();
        Notification retrying = new Notification(NotificationChannel.EMAIL, "c@example.com", "C", "C");
        retrying.markAsRetrying(0);
        Notification sent = new Notification(NotificationChannel.EMAIL, "d@example.com", "D", "D");
        sent.markAsProcessing();
        sent.markAsSent();
        Notification failed = new Notification(NotificationChannel.EMAIL, "e@example.com", "E", "E");
        failed.markAsProcessing();
        failed.markAsFailed();

        repository.save(pending);
        repository.save(processing);
        repository.save(retrying);
        repository.save(sent);
        repository.save(failed);

        NotificationDashboardResponse response = useCase.execute();

        assertEquals(5, response.total());
        assertEquals(1, response.pending());
        assertEquals(1, response.processing());
        assertEquals(1, response.retrying());
        assertEquals(3, response.pendingTotal());
        assertEquals(1, response.sent());
        assertEquals(1, response.failed());
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
