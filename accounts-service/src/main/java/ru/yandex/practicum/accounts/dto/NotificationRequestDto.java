package ru.yandex.practicum.accounts.dto;

public record NotificationRequestDto(
        String userLogin,
        String message,
        String type
) {
}