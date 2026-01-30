package com.github.RenanAlmeida225.notification_service.useCases.notification;

import com.github.RenanAlmeida225.notification_service.infra.messaging.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PublishNotificationUseCase {
    private final RabbitTemplate rabbitTemplate;

    public PublishNotificationUseCase(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(UUID notificationId) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PROCESS_NOTIFICATION_QUEUE,
                notificationId.toString()
        );
    }
}
