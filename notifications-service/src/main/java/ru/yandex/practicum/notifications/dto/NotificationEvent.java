package ru.yandex.practicum.notifications.dto;

public record NotificationEvent(
        String userLogin,
        String message,
        String type
) {
}