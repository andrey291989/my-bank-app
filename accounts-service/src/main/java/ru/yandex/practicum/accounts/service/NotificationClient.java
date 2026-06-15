package ru.yandex.practicum.accounts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.accounts.dto.NotificationRequestDto;

@Service
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final WebClient webClient;

    public NotificationClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public void sendNotification(String userLogin, String message, String type) {
        try {
            var request = new NotificationRequestDto(userLogin, message, type);
            webClient.post()
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