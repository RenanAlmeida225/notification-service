package com.github.RenanAlmeida225.notification_service;

import com.github.RenanAlmeida225.notification_service.api.controllers.dto.SendNotificationResponse;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationChannel;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("fake")
class NotificationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("notification_db")
            .withUsername("notification")
            .withPassword("notification");

    @Container
    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3.12-management")
            .withExposedPorts(5672);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", () -> RABBIT.getMappedPort(5672));
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void endToEnd_sendNotification_thenStatusIsSent() throws Exception {
        Map<String, Object> request = Map.of(
                "channel", NotificationChannel.EMAIL.name(),
                "recipient", "user@example.com",
                "title", "Hello",
                "message", "Test message"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        SendNotificationResponse response = restTemplate.postForObject(
                "http://localhost:" + port + "/api/v1/notifications",
                entity,
                SendNotificationResponse.class
        );

        assertNotNull(response);
        UUID id = response.id();
        assertNotNull(id);

        Notification notification = waitForStatus(id, NotificationStatus.SENT, Duration.ofSeconds(5));
        assertEquals(NotificationStatus.SENT, notification.getStatus());
    }

    @Test
    void endToEnd_idempotencyKey_returnsSameId() {
        Map<String, Object> request = Map.of(
                "channel", NotificationChannel.EMAIL.name(),
                "recipient", "user@example.com",
                "title", "Hello",
                "message", "Test message"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Idempotency-Key", "key-123");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        SendNotificationResponse first = restTemplate.postForObject(
                "http://localhost:" + port + "/api/v1/notifications",
                entity,
                SendNotificationResponse.class
        );

        SendNotificationResponse second = restTemplate.postForObject(
                "http://localhost:" + port + "/api/v1/notifications",
                entity,
                SendNotificationResponse.class
        );

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.id(), second.id());
    }

    @Test
    void endToEnd_processingQueue_isConsumed() throws Exception {
        Map<String, Object> request = Map.of(
                "channel", NotificationChannel.EMAIL.name(),
                "recipient", "user@example.com",
                "title", "Hello",
                "message", "Test message"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        SendNotificationResponse response = restTemplate.postForObject(
                "http://localhost:" + port + "/api/v1/notifications",
                entity,
                SendNotificationResponse.class
        );

        assertNotNull(response);
        UUID id = response.id();
        assertNotNull(id);

        Notification notification = waitForStatus(id, NotificationStatus.SENT, Duration.ofSeconds(5));
        assertEquals(NotificationStatus.SENT, notification.getStatus());

        Object message = rabbitTemplate.receiveAndConvert("notification.process.queue");
        assertNull(message);
    }

    private Notification waitForStatus(UUID id, NotificationStatus status, Duration timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        Notification current = null;
        while (System.currentTimeMillis() < deadline) {
            current = restTemplate.getForObject(
                    "http://localhost:" + port + "/api/v1/notifications/" + id,
                    Notification.class
            );
            if (current != null && current.getStatus() == status) {
                return current;
            }
            Thread.sleep(200);
        }
        return current;
    }
}
import org.springframework.amqp.rabbit.core.RabbitTemplate;
