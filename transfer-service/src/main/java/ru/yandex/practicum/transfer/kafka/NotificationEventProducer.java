package ru.yandex.practicum.transfer.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.transfer.dto.NotificationEvent;

@Service
public class NotificationEventProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventProducer.class);
    private static final String NOTIFICATIONS_TOPIC = "notifications";

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public NotificationEventProducer(KafkaTemplate<String, NotificationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(String userLogin, String message, String type) {
        try {
            NotificationEvent event = new NotificationEvent(userLogin, message, type);
            kafkaTemplate.send(NOTIFICATIONS_TOPIC, userLogin, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Notification sent to Kafka topic {}: user={}, message={}, type={}",
                                    NOTIFICATIONS_TOPIC, userLogin, message, type);
                        } else {
                            log.error("Failed to send notification to Kafka topic {}: user={}, message={}, type={}",
                                    NOTIFICATIONS_TOPIC, userLogin, message, type, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending notification to Kafka: user={}, message={}, type={}",
                    userLogin, message, type, e);
        }
    }
}