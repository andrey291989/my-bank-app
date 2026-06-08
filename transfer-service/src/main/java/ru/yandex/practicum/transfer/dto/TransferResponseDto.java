package ru.yandex.practicum.transfer.dto;

public record TransferResponseDto(
        String fromLogin,
        String fromName,
        String toLogin,
        String toName,
        Integer amount,
        Integer newBalance,
        String message
) {
}