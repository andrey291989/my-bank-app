package ru.yandex.practicum.cash.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CashRequestDto(
        @NotBlank(message = "Login is required")
        String login,

        @NotNull(message = "Value is required")
        @Min(value = 1, message = "Amount must be positive")
        Integer value,

        @NotNull(message = "Action is required")
        CashAction action
) {
    public enum CashAction {
        PUT, GET
    }
}