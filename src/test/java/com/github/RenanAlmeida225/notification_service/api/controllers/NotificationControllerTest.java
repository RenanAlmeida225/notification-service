package com.github.RenanAlmeida225.notification_service.api.controllers;

import com.github.RenanAlmeida225.notification_service.api.controllers.dto.NotificationDashboardResponse;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationChannel;
import com.github.RenanAlmeida225.notification_service.useCases.notification.NotificationDashboardUseCase;
import com.github.RenanAlmeida225.notification_service.useCases.notification.SendNotificationUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationControllerTest {

    @Test
    void postNotification_returnsCreatedWithId() throws Exception {
        UUID expectedId = UUID.randomUUID();
        SendNotificationUseCase sendUseCase = new StubSendNotificationUseCase(expectedId);
        NotificationDashboardUseCase dashboardUseCase = new StubNotificationDashboardUseCase();
        NotificationController controller = new NotificationController(sendUseCase, dashboardUseCase);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        String json = """
                {
                  "channel": "EMAIL",
                  "recipient": "user@example.com",
                  "title": "Hello",
                  "message": "Test message"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string(expectedId.toString()));
    }

    @Test
    void dashboard_returnsCounts() throws Exception {
        SendNotificationUseCase sendUseCase = new StubSendNotificationUseCase(UUID.randomUUID());
        NotificationDashboardUseCase dashboardUseCase = new StubNotificationDashboardUseCase();
        NotificationController controller = new NotificationController(sendUseCase, dashboardUseCase);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/notifications/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.pending").value(4))
                .andExpect(jsonPath("$.processing").value(2))
                .andExpect(jsonPath("$.retrying").value(1))
                .andExpect(jsonPath("$.sent").value(2))
                .andExpect(jsonPath("$.failed").value(1));
    }

    private static class StubSendNotificationUseCase extends SendNotificationUseCase {
        private final UUID id;

        StubSendNotificationUseCase(UUID id) {
            super(null, null);
            this.id = id;
        }

        @Override
        public UUID execute(Notification notification) {
            return id;
        }

        @Override
        public Optional<Notification> findById(UUID id) {
            Notification notification = new Notification(
                    NotificationChannel.EMAIL,
                    "user@example.com",
                    "Hello",
                    "Test message"
            );
            return Optional.of(notification);
        }

        @Override
        public List<Notification> findAll() {
            return List.of();
        }
    }

    private static class StubNotificationDashboardUseCase extends NotificationDashboardUseCase {
        StubNotificationDashboardUseCase() {
            super(null);
        }

        @Override
        public NotificationDashboardResponse execute() {
            return new NotificationDashboardResponse(10, 4, 2, 1, 2, 1);
        }
    }
}
