package ru.yandex.practicum.cash.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.shared.kafka.producer.NotificationEventProducer;

@Service
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final NotificationEventProducer notificationEventProducer;

    public NotificationClient(NotificationEventProducer notificationEventProducer) {
        this.notificationEventProducer = notificationEventProducer;
    }

    public void sendNotification(String userLogin, String message, String type) {
        try {
            notificationEventProducer.sendNotification(userLogin, message, type);
        } catch (Exception e) {
            log.error("Error sending notification via Kafka: {}", e.getMessage());
        }
    }
}