package com.github.RenanAlmeida225.notification_service.useCases.sender;

import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("fake")
@Component
public class FakeNotificationSender implements NotificationSender {
    @Override
    public void send(Notification notification) {
//        throw new RuntimeException("Erro forçado para teste de retry");
        System.out.println(
                "Sending " + notification.getChannel() +
                        " to " + notification.getRecipient()
        );
    }
}
