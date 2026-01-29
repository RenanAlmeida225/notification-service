package com.github.RenanAlmeida225.notification_service.api.controllers;

import com.github.RenanAlmeida225.notification_service.api.controllers.dto.NotificationDashboardResponse;
import com.github.RenanAlmeida225.notification_service.api.controllers.dto.SendNotificationRequest;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.useCases.notification.NotificationDashboardUseCase;
import com.github.RenanAlmeida225.notification_service.useCases.notification.SendNotificationUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final SendNotificationUseCase useCase;
    private final NotificationDashboardUseCase dashboardUseCase;


    public NotificationController(SendNotificationUseCase useCase, NotificationDashboardUseCase dashboardUseCase) {
        this.useCase = useCase;
        this.dashboardUseCase = dashboardUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String send(@RequestBody SendNotificationRequest request) {
        Notification notification = new Notification(
                request.channel(),
                request.recipient(),
                request.title(),
                request.message()
        );

        return useCase.execute(notification).toString();
    }

    @GetMapping
    public List<Notification> list() {
        return useCase.findAll();
    }

    @GetMapping("/dashboard")
    public NotificationDashboardResponse dashboard() {
        return dashboardUseCase.execute();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Notification status(@PathVariable UUID id) {
        return useCase.findById(id).orElseThrow(() -> new RuntimeException("Notification not found"));
    }

}
