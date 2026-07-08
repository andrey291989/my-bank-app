package ru.yandex.practicum.accounts.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.accounts.kafka.NotificationEventProducer;
import ru.yandex.practicum.accounts.service.NotificationClient;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationClientTest {

    @Mock
    private NotificationEventProducer notificationEventProducer;

    private NotificationClient notificationClient;

    @BeforeEach
    void setUp() {
        notificationClient = new NotificationClient(notificationEventProducer);
    }

    @Test
    void testNotificationFlowThroughKafka() {
        // Arrange
        String userLogin = "testuser";
        String message = "Account created successfully";
        String type = "ACCOUNT_CREATED";

        // Act
        notificationClient.sendNotification(userLogin, message, type);

        // Assert
        verify(notificationEventProducer, times(1))
                .sendNotification(userLogin, message, type);
    }

    @Test
    void testNotificationFlowWithException() {
        // Arrange
        String userLogin = "testuser";
        String message = "Account created successfully";
        String type = "ACCOUNT_CREATED";

        // Mock the producer to throw an exception
        doThrow(new RuntimeException("Kafka error"))
                .when(notificationEventProducer).sendNotification(userLogin, message, type);

        // Act
        notificationClient.sendNotification(userLogin, message, type);

        // Assert
        verify(notificationEventProducer, times(1))
                .sendNotification(userLogin, message, type);
    }
}