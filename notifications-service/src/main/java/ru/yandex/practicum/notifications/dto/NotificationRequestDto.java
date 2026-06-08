package ru.yandex.practicum.notifications.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationRequestDto(
        @NotBlank(message = "User login is required")
        String userLogin,

        @NotBlank(message = "Message is required")
        String message,

        @NotBlank(message = "Notification type is required")
        String type
) {
}