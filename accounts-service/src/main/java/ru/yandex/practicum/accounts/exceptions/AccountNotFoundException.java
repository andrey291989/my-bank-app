package ru.yandex.practicum.accounts.exceptions;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String login) {
        super(login);
    }

    public AccountNotFoundException(String login, Throwable cause) {
        super("Account not found: " + login, cause);
    }
}