package ru.yandex.practicum.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.notifications.dto.NotificationRequestDto;
import ru.yandex.practicum.notifications.model.Notification;
import ru.yandex.practicum.notifications.repository.NotificationRepository;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final String defaultDeliveryMethod;

    public NotificationService(NotificationRepository notificationRepository, EmailService emailService,
                              @Value("${notifications.delivery.method:LOG}") String defaultDeliveryMethod) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.defaultDeliveryMethod = defaultDeliveryMethod;
    }

    @Transactional
    public void sendNotification(NotificationRequestDto request) {
        String deliveryMethod = defaultDeliveryMethod;

        // Save to database first
        Notification notification = new Notification(
                request.userLogin(),
                request.message(),
                request.type(),
                deliveryMethod
        );
        notification = notificationRepository.save(notification);

        boolean success = false;
        String errorMessage = null;

        if ("EMAIL".equalsIgnoreCase(deliveryMethod)) {
            String userEmail = emailService.getUserEmail(request.userLogin());
            String subject = "Bank Notification: " + request.type();
            success = emailService.sendEmail(userEmail, subject, request.message());
            if (!success) {
                errorMessage = "Email delivery failed";
            }
        } else {
            // Default: write to log
            logNotification(notification);
            success = true;
        }

        // Update notification status
        if (success) {
            notification.setDeliveryStatus("SUCCESS");
            notification.setSentAt(LocalDateTime.now());
        } else {
            notification.setDeliveryStatus("FAILED");
            notification.setErrorMessage(errorMessage);
        }
        notificationRepository.save(notification);
    }

    private void logNotification(Notification notification) {
        log.info("=== NOTIFICATION ===");
        log.info("To: {}", notification.getUserLogin());
        log.info("Type: {}", notification.getType());
        log.info("Message: {}", notification.getMessage());
        log.info("Time: {}", notification.getCreatedAt());
        log.info("===================");
    }
}