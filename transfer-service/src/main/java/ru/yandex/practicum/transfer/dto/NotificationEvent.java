package ru.yandex.practicum.transfer.dto;

public record NotificationEvent(
        String userLogin,
        String message,
        String type
) {
}