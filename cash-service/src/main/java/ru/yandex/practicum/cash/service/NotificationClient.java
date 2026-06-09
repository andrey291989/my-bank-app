package ru.yandex.practicum.cash.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.cash.dto.NotificationRequestDto;

@Service
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final WebClient notificationsWebClient;

    public NotificationClient(@Qualifier("notificationsWebClient") WebClient notificationsWebClient) {
        this.notificationsWebClient = notificationsWebClient;
    }

    public void sendNotification(String userLogin, String message, String type) {
        try {
            var request = new NotificationRequestDto(userLogin, message, type);
            notificationsWebClient.post()
                    .uri("/api/notifications")
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(response -> log.info("Notification sent to {}: {}", userLogin, message))
                    .doOnError(error -> log.error("Failed to send notification: {}", error.getMessage()))
                    .subscribe();
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage());
        }
    }
}