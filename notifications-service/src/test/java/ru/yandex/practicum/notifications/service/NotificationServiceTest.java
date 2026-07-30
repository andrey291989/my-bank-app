package ru.yandex.practicum.notifications.service;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.notifications.dto.NotificationRequestDto;
import ru.yandex.practicum.notifications.model.Notification;
import ru.yandex.practicum.notifications.repository.NotificationRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private EmailService emailService;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void sendNotification_logDelivery_shouldSucceedWithoutFailureMetric() {
        var service = new NotificationService(notificationRepository, emailService, "LOG", meterRegistry);

        service.sendNotification(new NotificationRequestDto("user", "hello", "INFO"));

        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
        // Метрика неуспешных отправок не должна расти при успешной доставке
        assertEquals(0.0, meterRegistry.counter("bank.notification.send.failed", "login", "user").count());
    }

    @Test
    void sendNotification_emailFailure_shouldIncrementFailureMetric() {
        when(emailService.getUserEmail("user")).thenReturn("user@example.com");
        when(emailService.sendEmail(any(), any(), any())).thenReturn(false);

        var service = new NotificationService(notificationRepository, emailService, "EMAIL", meterRegistry);

        service.sendNotification(new NotificationRequestDto("user", "hello", "INFO"));

        // Кастомная бизнес-метрика: невозможность отправки уведомления (по логину)
        assertEquals(1.0, meterRegistry.counter("bank.notification.send.failed", "login", "user").count());
    }
}
