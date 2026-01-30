package com.github.RenanAlmeida225.notification_service.infra.messaging;

import com.github.RenanAlmeida225.notification_service.useCases.notification.ProcessNotificationUseCase;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProcessNotificationListener {
    private final ProcessNotificationUseCase useCase;

    public ProcessNotificationListener(ProcessNotificationUseCase useCase) {
        this.useCase = useCase;
    }

    @RabbitListener(queues = RabbitMQConfig.PROCESS_NOTIFICATION_QUEUE)
    public void consume(String notificationId) {
        useCase.execute(UUID.fromString(notificationId));
    }
}
