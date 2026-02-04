package com.github.RenanAlmeida225.notification_service.api.controllers.dto;

import java.util.Map;

public record ErrorResponse(
        String error,
        Map<String, String> fields
) {
}
