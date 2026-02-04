package com.github.RenanAlmeida225.notification_service.infra.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class NotificationMetrics {
    private final Counter created;
    private final Counter sent;
    private final Counter failed;
    private final Counter retrying;

    public NotificationMetrics(MeterRegistry registry) {
        this.created = registry.counter("notification.created");
        this.sent = registry.counter("notification.sent");
        this.failed = registry.counter("notification.failed");
        this.retrying = registry.counter("notification.retrying");
    }

    public void incrementCreated() {
        created.increment();
    }

    public void incrementSent() {
        sent.increment();
    }

    public void incrementFailed() {
        failed.increment();
    }

    public void incrementRetrying() {
        retrying.increment();
    }
}
