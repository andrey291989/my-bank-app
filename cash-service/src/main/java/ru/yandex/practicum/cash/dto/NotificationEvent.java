package ru.yandex.practicum.cash.dto;

public record NotificationEvent(
        String userLogin,
        String message,
        String type
) {
}