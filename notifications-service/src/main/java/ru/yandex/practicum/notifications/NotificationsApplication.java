package ru.yandex.practicum.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.shared.kafka.config.SharedKafkaConfig;

@SpringBootApplication
@EnableDiscoveryClient
@Import(SharedKafkaConfig.class)
public class NotificationsApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationsApplication.class, args);
    }
}