package ru.yandex.practicum.transfer.dto;

import java.math.BigDecimal;

public record TransferResponseDto(
        String fromLogin,
        String fromName,
        String toLogin,
        String toName,
        BigDecimal amount,
        BigDecimal newBalance,
        String message
) {
}