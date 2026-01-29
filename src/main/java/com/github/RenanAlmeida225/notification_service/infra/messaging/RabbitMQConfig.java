package com.github.RenanAlmeida225.notification_service.infra.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
    public static final String PROCESS_NOTIFICATION_QUEUE = "notification.process.queue";

    @Bean
    public Queue processNotificationQueue() {
        return new Queue(PROCESS_NOTIFICATION_QUEUE, true);
    }
}
