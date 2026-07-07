package ru.yandex.practicum.notifications.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.support.Acknowledgment;
import ru.yandex.practicum.notifications.dto.NotificationEvent;
import ru.yandex.practicum.notifications.dto.NotificationRequestDto;
import ru.yandex.practicum.notifications.service.NotificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class NotificationEventListenerTest {

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private Acknowledgment acknowledgment;

    @Test
    void testHandleNotificationEvent() {
        // Arrange
        NotificationEventListener listener = new NotificationEventListener(notificationService);
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
}