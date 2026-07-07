package ru.yandex.practicum.accounts.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import ru.yandex.practicum.accounts.dto.NotificationEvent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class NotificationEventProducerTest {

    @MockBean
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Test
    void testSendNotification() {
        // Arrange
        NotificationEventProducer producer = new NotificationEventProducer(kafkaTemplate);
        String userLogin = "testuser";
        String message = "Test message";
        String type = "INFO";

        // Mock the Kafka template send method
        when(kafkaTemplate.send(anyString(), anyString(), any(NotificationEvent.class)))
                .thenReturn(null);

        // Act
        producer.sendNotification(userLogin, message, type);

        // Assert
        verify(kafkaTemplate, times(1))
                .send(eq("notifications"), eq(userLogin), any(NotificationEvent.class));
    }
}