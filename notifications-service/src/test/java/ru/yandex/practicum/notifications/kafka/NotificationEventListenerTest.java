package ru.yandex.practicum.notifications.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import ru.yandex.practicum.shared.kafka.dto.NotificationEvent;
import ru.yandex.practicum.notifications.dto.NotificationRequestDto;
import ru.yandex.practicum.notifications.service.NotificationService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Acknowledgment acknowledgment;

    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new NotificationEventListener(notificationService);
    }

    @Test
    void testHandleNotificationEvent() {
        // Arrange
        String userLogin = "testuser";
        String message = "Test message";
        String type = "INFO";
        NotificationEvent event = new NotificationEvent(userLogin, message, type);
        ConsumerRecord<String, NotificationEvent> record = new ConsumerRecord<>("notifications", 0, 0, userLogin, event);

        // Mock the notification service
        doNothing().when(notificationService).sendNotification(any(NotificationRequestDto.class));

        // Act
        listener.handleNotificationEvent(record, acknowledgment);

        // Assert
        verify(notificationService, times(1)).sendNotification(any(NotificationRequestDto.class));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void testHandleNotificationEventWithException() {
        // Arrange
        String userLogin = "testuser";
        String message = "Test message";
        String type = "INFO";
        NotificationEvent event = new NotificationEvent(userLogin, message, type);
        ConsumerRecord<String, NotificationEvent> record = new ConsumerRecord<>("notifications", 0, 0, userLogin, event);

        // Mock the notification service to throw an exception
        doThrow(new RuntimeException("Service error"))
                .when(notificationService).sendNotification(any(NotificationRequestDto.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            listener.handleNotificationEvent(record, acknowledgment);
        });

        // Verify that acknowledgment was not called
        verify(notificationService, times(1)).sendNotification(any(NotificationRequestDto.class));
        verify(acknowledgment, never()).acknowledge();
    }
}