package com.github.RenanAlmeida225.notification_service.infra.worker;

import com.github.RenanAlmeida225.notification_service.infra.database.repositories.NotificationRepository;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.models.notification.NotificationStatus;
import com.github.RenanAlmeida225.notification_service.useCases.notification.ProcessNotificationUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationWorker {
    private final NotificationRepository repository;
    private final ProcessNotificationUseCase processUseCase;


    public NotificationWorker(NotificationRepository repository, ProcessNotificationUseCase processUseCase) {
        this.repository = repository;
        this.processUseCase = processUseCase;
    }

    @Scheduled(fixedDelay = 5000)
    public void processPendingNotifications() {

        List<Notification> notifications =
                repository.findByStatusIn(
                        List.of(
                                NotificationStatus.PENDING,
                                NotificationStatus.RETRYING
                        )
                );


        for (Notification notification : notifications) {
            processUseCase.execute(notification.getId());
        }
    }

}
