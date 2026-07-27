package ru.yandex.practicum.cash.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CashRequestDto(
        @NotBlank(message = "Login is required")
        String login,

        @NotNull(message = "Value is required")
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places")
        BigDecimal value,

        @NotNull(message = "Action is required")
        CashAction action
) {
    public enum CashAction {
        PUT, GET
    }
}