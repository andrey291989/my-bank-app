package ru.yandex.practicum.accounts.exceptions;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(BigDecimal balance, BigDecimal requestedAmount) {
        super("Insufficient funds: balance=%s, requested=%s".formatted(balance, requestedAmount));
    }
}