package ru.yandex.practicum.mybankfront.controller.dto;

import java.math.BigDecimal;

public record CashRequestDto(String login, BigDecimal value, CashAction action) {
}