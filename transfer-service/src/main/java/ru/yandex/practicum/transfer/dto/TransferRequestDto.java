package ru.yandex.practicum.transfer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferRequestDto(
        @NotBlank(message = "From login is required")
        String fromLogin,

        @NotBlank(message = "To login is required")
        String toLogin,

        @NotNull(message = "Amount is required")
        @Min(value = 1, message = "Amount must be positive")
        Integer amount
) {
}