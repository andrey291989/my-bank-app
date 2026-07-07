package ru.yandex.practicum.accounts.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.accounts.kafka.NotificationEventProducer;
import ru.yandex.practicum.accounts.service.NotificationClient;

import static org.mockito.Mockito.*;

@SpringBootTest
@Import({NotificationEventProducer.class})
class KafkaIntegrationTest {

    @MockBean
    private NotificationEventProducer notificationEventProducer;

    @Test
    void testNotificationFlowThroughKafka() {
        // Arrange
        NotificationClient notificationClient = new NotificationClient(notificationEventProducer);
        String userLogin = "testuser";
        String message = "Account created successfully";
        String type = "ACCOUNT_CREATED";

        // Act
        notificationClient.sendNotification(userLogin, message, type);

        // Assert
        verify(notificationEventProducer, times(1))
                .sendNotification(userLogin, message, type);
    }
}