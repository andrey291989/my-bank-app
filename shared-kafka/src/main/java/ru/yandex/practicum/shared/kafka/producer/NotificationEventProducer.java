package ru.yandex.practicum.shared.kafka.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.shared.kafka.dto.NotificationEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Shared Kafka producer for sending notification events
 */
@Component
public class NotificationEventProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventProducer.class);
    private static final String NOTIFICATIONS_TOPIC = "notifications";

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public NotificationEventProducer(KafkaTemplate<String, NotificationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends a notification event to the Kafka topic
     *
     * @param userLogin the user login
     * @param message   the notification message
     * @param type      the notification type
     */
    public void sendNotification(String userLogin, String message, String type) {
        try {
            NotificationEvent event = new NotificationEvent(userLogin, message, type);
            CompletableFuture<Void> future = kafkaTemplate.send(NOTIFICATIONS_TOPIC, userLogin, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Notification sent to Kafka topic {}: user={}, message={}, type={}",
                                    NOTIFICATIONS_TOPIC, userLogin, message, type);
                        } else {
                            log.error("Failed to send notification to Kafka topic {}: user={}, message={}, type={}",
                                    NOTIFICATIONS_TOPIC, userLogin, message, type, ex);
                        }
                    })
                    .thenAccept(result -> {});

            // Handle exceptions during send operation
            future.exceptionally(ex -> {
                log.error("Error sending notification to Kafka: user={}, message={}, type={}",
                        userLogin, message, type, ex);
                return null;
            });
        } catch (Exception e) {
            log.error("Error sending notification to Kafka: user={}, message={}, type={}",
                    userLogin, message, type, e);
        }
    }

    /**
     * Gets the notifications topic name
     *
     * @return the topic name
     */
    public static String getNotificationsTopic() {
        return NOTIFICATIONS_TOPIC;
    }
}