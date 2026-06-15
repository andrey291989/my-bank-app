package ru.yandex.practicum.accounts.exceptions;

public class UnderAgeException extends RuntimeException {
    public UnderAgeException(String message) {
        super(message);
    }

    public UnderAgeException(int age) {
        super("User must be over 18 years old. Current age: " + age);
    }
}