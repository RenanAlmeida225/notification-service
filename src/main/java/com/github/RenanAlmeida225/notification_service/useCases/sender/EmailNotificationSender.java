package com.github.RenanAlmeida225.notification_service.useCases.sender;

import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements NotificationSender {
    private final JavaMailSender mailSender;
    private final String from;

    public EmailNotificationSender(
            JavaMailSender mailSender,
            @Value("${notification.email.from:no-reply@example.com}") String from
    ) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(Notification notification) {
        if (notification.getChannel() != NotificationChannel.EMAIL) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(notification.getRecipient());
        message.setSubject(notification.getTitle());
        message.setText(notification.getMessage());
        mailSender.send(message);
    }
}
