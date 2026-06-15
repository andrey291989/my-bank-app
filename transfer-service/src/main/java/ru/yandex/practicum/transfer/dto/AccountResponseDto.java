package ru.yandex.practicum.transfer.dto;

import java.math.BigDecimal;

public record AccountResponseDto(
        String login,
        String name,
        String birthdate,
        BigDecimal sum
) {
}