package ru.yandex.practicum.cash.dto;

public record CashResponseDto(
        String login,
        String name,
        String birthdate,
        Integer sum,
        String message
) {
}