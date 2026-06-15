package ru.yandex.practicum.transfer.dto;

public record NotificationRequestDto(
        String userLogin,
        String message,
        String type
) {
}