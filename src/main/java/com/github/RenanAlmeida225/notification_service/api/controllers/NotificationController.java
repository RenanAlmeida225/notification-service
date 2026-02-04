package com.github.RenanAlmeida225.notification_service.api.controllers;

import com.github.RenanAlmeida225.notification_service.api.controllers.dto.NotificationDashboardResponse;
import com.github.RenanAlmeida225.notification_service.api.controllers.dto.SendNotificationRequest;
import com.github.RenanAlmeida225.notification_service.api.controllers.dto.SendNotificationResponse;
import com.github.RenanAlmeida225.notification_service.models.notification.Notification;
import com.github.RenanAlmeida225.notification_service.useCases.notification.NotificationDashboardUseCase;
import com.github.RenanAlmeida225.notification_service.useCases.notification.SendNotificationUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

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
    public SendNotificationResponse send(
            @Valid @RequestBody SendNotificationRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        Notification notification = new Notification(
                request.channel(),
                request.recipient(),
                request.title(),
                request.message()
        );

        UUID id = useCase.execute(notification, idempotencyKey);
        Notification saved = useCase.findById(id).orElse(notification);
        return new SendNotificationResponse(id, saved.getStatus());
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
        return useCase.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
    }

}
