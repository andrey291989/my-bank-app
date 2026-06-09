package ru.yandex.practicum.cash.dto;

import java.math.BigDecimal;

public record CashResponseDto(
        String login,
        String name,
        String birthdate,
        BigDecimal sum,
        String message
) {
}