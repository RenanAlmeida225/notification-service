package com.github.RenanAlmeida225.notification_service.models.notification;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Version
    private Long version;

    private String recipient;
    private String title;
    private String message;
    private int attempts;
    private Instant lastAttemptAt;
    private Instant createdAt;


    protected Notification() {
        // JPA only
    }


    public Notification(
            NotificationChannel channel,
            String recipient,
            String title,
            String message
    ) {
        this.id = UUID.randomUUID();
        this.channel = channel;
        this.recipient = recipient;
        this.title = title;
        this.message = message;
        this.status = NotificationStatus.PENDING;
        this.attempts = 0;
        this.lastAttemptAt = null;
        this.createdAt = Instant.now();

    }

    public void markAsSent() {
        if (this.status != NotificationStatus.PROCESSING) {
            throw new IllegalStateException("Only PROCESSING notifications can be sent");
        }
        this.status = NotificationStatus.SENT;
    }

    public void markAsFailed() {
        if (this.status != NotificationStatus.PROCESSING) {
            throw new IllegalStateException("Only PROCESSING notifications can fail");
        }
        this.status = NotificationStatus.FAILED;
    }

    public void markAsProcessing() {
        if (this.status != NotificationStatus.PENDING &&
                this.status != NotificationStatus.FAILED) {
            throw new IllegalStateException("Notification cannot be processed");
        }
        this.status = NotificationStatus.PROCESSING;
        this.attempts++;
        this.lastAttemptAt = Instant.now();
    }

    public void markAsRetrying() {
        this.status = NotificationStatus.RETRYING;
    }


    public boolean canRetry(int maxAttempts) {
        return this.attempts < maxAttempts &&
                this.status != NotificationStatus.FAILED;
    }


    public boolean canBeProceed() {
        return this.status == NotificationStatus.PENDING
                || this.status == NotificationStatus.FAILED;
    }

    public boolean canRetryNow(int maxAttempts) {
        if (this.attempts >= maxAttempts) {
            return false;
        }

        if (this.lastAttemptAt == null) {
            return true;
        }

        long backoffSeconds = calculateBackoffSeconds();
        return lastAttemptAt.plusSeconds(backoffSeconds).isBefore(Instant.now());
    }

    private long calculateBackoffSeconds() {
        // backoff exponencial simples: 5s, 15s, 45s...
        return (long) Math.pow(3, attempts) * 5;
    }


    public UUID getId() {
        return id;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
