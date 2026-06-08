package ru.yandex.practicum.notifications.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.notifications.model.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserLoginOrderByCreatedAtDesc(String userLogin);
    List<Notification> findByDeliveryStatus(String status);
}