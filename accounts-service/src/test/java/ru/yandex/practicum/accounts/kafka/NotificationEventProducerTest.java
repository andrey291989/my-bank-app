package ru.yandex.practicum.accounts.kafka;

import ru.yandex.practicum.shared.kafka.producer.NotificationEventProducer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import ru.yandex.practicum.shared.kafka.dto.NotificationEvent;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventProducerTest {

    @Mock
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    private NotificationEventProducer producer;

    @BeforeEach
    void setUp() {
        producer = new NotificationEventProducer(kafkaTemplate);
    }

    @Test
    void testSendNotification() {
        // Arrange
        String userLogin = "testuser";
        String message = "Test message";
        String type = "INFO";

        // Mock the Kafka template send method
        CompletableFuture<SendResult<String, NotificationEvent>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any(NotificationEvent.class)))
                .thenReturn(future);

        // Act
        producer.sendNotification(userLogin, message, type);

        // Assert
        verify(kafkaTemplate, times(1))
                .send(eq("notifications"), eq(userLogin), any(NotificationEvent.class));
    }

    @Test
    void testSendNotificationWithException() {
        // Arrange
        String userLogin = "testuser";
        String message = "Test message";
        String type = "INFO";

        // Mock the Kafka template send method to throw an exception
        when(kafkaTemplate.send(anyString(), anyString(), any(NotificationEvent.class)))
                .thenThrow(new RuntimeException("Kafka error"));

        // Act
        producer.sendNotification(userLogin, message, type);

        // Assert
        verify(kafkaTemplate, times(1))
                .send(eq("notifications"), eq(userLogin), any(NotificationEvent.class));
    }
}