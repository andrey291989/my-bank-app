package ru.yandex.practicum.cash.dto;

public record NotificationRequestDto(
        String userLogin,
        String message,
        String type
) {
}