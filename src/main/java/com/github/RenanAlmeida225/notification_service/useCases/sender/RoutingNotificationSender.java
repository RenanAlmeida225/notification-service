package com.github.RenanAlmeida225.notification_service.useCases.sender;

import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationChannel;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Primary
@Component
public class RoutingNotificationSender implements NotificationSender {
    private final Map<NotificationChannel, NotificationSender> delegates;

    public RoutingNotificationSender(List<NotificationSender> senders) {
        Map<NotificationChannel, NotificationSender> map = new EnumMap<>(NotificationChannel.class);
        for (NotificationSender sender : senders) {
            if (sender instanceof EmailNotificationSender) {
                map.put(NotificationChannel.EMAIL, sender);
            }
        }
        this.delegates = map;
    }

    @Override
    public void send(Notification notification) {
        NotificationSender sender = delegates.get(notification.getChannel());
        if (sender == null) {
            throw new IllegalStateException("No sender configured for channel " + notification.getChannel());
        }
        sender.send(notification);
    }
}
