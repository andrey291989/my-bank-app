package ru.yandex.practicum.notifications.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.shared.kafka.KafkaTopics;
import ru.yandex.practicum.shared.kafka.dto.NotificationEvent;
import ru.yandex.practicum.notifications.service.NotificationService;

@Service
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);
    private static final String NOTIFICATIONS_TOPIC = KafkaTopics.NOTIFICATIONS;

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = NOTIFICATIONS_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleNotificationEvent(ConsumerRecord<String, NotificationEvent> record, Acknowledgment ack) {
        try {
            NotificationEvent event = record.value();
            log.info("Received notification event from Kafka: user={}, message={}, type={}",
                    event.userLogin(), event.message(), event.type());

            // Convert NotificationEvent to NotificationRequestDto
            var request = new ru.yandex.practicum.notifications.dto.NotificationRequestDto(
                    event.userLogin(), event.message(), event.type());

            // Process the notification
            notificationService.sendNotification(request);

            // Acknowledge the message only after successful processing
            ack.acknowledge();
            log.info("Successfully processed notification event for user: {}", event.userLogin());
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage(), e);
            // Don't acknowledge the message, it will be redelivered
            throw e;
        }
    }
}