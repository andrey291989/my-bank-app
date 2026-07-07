package ru.yandex.practicum.accounts.dto;

public record NotificationEvent(
        String userLogin,
        String message,
        String type
) {
}