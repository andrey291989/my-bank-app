package ru.yandex.practicum.shared.kafka.dto;

/**
 * DTO for notification events sent through Kafka
 */
public record NotificationEvent(
        String userLogin,
        String message,
        String type
) {
}