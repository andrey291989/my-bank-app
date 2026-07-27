package ru.yandex.practicum.shared.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import ru.yandex.practicum.shared.kafka.KafkaTopics;

/**
 * Общая конфигурация Kafka-топиков. Объявляется всеми сервисами, которые
 * работают с топиком уведомлений (и продюсерами, и консьюмером), поэтому топик
 * создаётся тем сервисом, который стартует первым, а не только notifications.
 * Параметры топика вынесены в конфигурацию.
 */
@Configuration
public class SharedKafkaConfig {

    @Bean
    public NewTopic notificationsTopic(
            @Value("${kafka.topics.notifications.partitions:3}") int partitions,
            @Value("${kafka.topics.notifications.replicas:1}") short replicas) {
        return TopicBuilder.name(KafkaTopics.NOTIFICATIONS)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
