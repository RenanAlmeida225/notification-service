package com.github.RenanAlmeida225.notification_service.api.controllers;

import com.github.RenanAlmeida225.notification_service.api.controllers.dto.NotificationDashboardResponse;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationChannel;
import com.github.RenanAlmeida225.notification_service.useCases.notification.NotificationDashboardUseCase;
import com.github.RenanAlmeida225.notification_service.useCases.notification.SendNotificationUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
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
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(buildValidator())
                .build();

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
                .andExpect(jsonPath("$.id").value(expectedId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void dashboard_returnsCounts() throws Exception {
        SendNotificationUseCase sendUseCase = new StubSendNotificationUseCase(UUID.randomUUID());
        NotificationDashboardUseCase dashboardUseCase = new StubNotificationDashboardUseCase();
        NotificationController controller = new NotificationController(sendUseCase, dashboardUseCase);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(buildValidator())
                .build();

        mockMvc.perform(get("/api/v1/notifications/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.pending").value(4))
                .andExpect(jsonPath("$.processing").value(2))
                .andExpect(jsonPath("$.retrying").value(1))
                .andExpect(jsonPath("$.pendingTotal").value(7))
                .andExpect(jsonPath("$.sent").value(2))
                .andExpect(jsonPath("$.failed").value(1));
    }

    @Test
    void postNotification_invalidEmail_returnsBadRequest() throws Exception {
        SendNotificationUseCase sendUseCase = new StubSendNotificationUseCase(UUID.randomUUID());
        NotificationDashboardUseCase dashboardUseCase = new StubNotificationDashboardUseCase();
        NotificationController controller = new NotificationController(sendUseCase, dashboardUseCase);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(buildValidator())
                .build();

        String json = """
                {
                  "channel": "EMAIL",
                  "recipient": "not-an-email",
                  "title": "Hello",
                  "message": "Test message"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields.recipient").exists());
    }

    private static LocalValidatorFactoryBean buildValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.afterPropertiesSet();
        return bean;
    }

    private static class StubSendNotificationUseCase extends SendNotificationUseCase {
        private final UUID id;

        StubSendNotificationUseCase(UUID id) {
            super(null, null, new com.github.RenanAlmeida225.notification_service.infra.metrics.NotificationMetrics(
                    new io.micrometer.core.instrument.simple.SimpleMeterRegistry()
            ));
            this.id = id;
        }

        @Override
        public UUID execute(Notification notification) {
            return id;
        }

        @Override
        public UUID execute(Notification notification, String idempotencyKey) {
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
            return new NotificationDashboardResponse(10, 4, 2, 1, 7, 2, 1);
        }
    }
}
