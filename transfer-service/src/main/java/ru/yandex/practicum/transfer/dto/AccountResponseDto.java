package ru.yandex.practicum.transfer.dto;

public record AccountResponseDto(
        String login,
        String name,
        String birthdate,
        Integer sum
) {
}