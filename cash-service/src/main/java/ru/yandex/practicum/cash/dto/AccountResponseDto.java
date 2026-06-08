package ru.yandex.practicum.cash.dto;

public record AccountResponseDto(
        String login,
        String name,
        String birthdate,
        Integer sum
) {
}