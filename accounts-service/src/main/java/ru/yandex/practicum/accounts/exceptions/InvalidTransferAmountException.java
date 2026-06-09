package ru.yandex.practicum.accounts.exceptions;

import java.math.BigDecimal;

public class InvalidTransferAmountException extends RuntimeException {
    public InvalidTransferAmountException(String message) {
        super(message);
    }

    public InvalidTransferAmountException(BigDecimal amount) {
        super("Transfer amount must be positive. Provided amount: " + amount);
    }
}