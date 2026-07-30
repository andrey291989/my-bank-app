package ru.yandex.practicum.shared.kafka;

/**
 * Общий контракт имён Kafka-топиков для всех сервисов приложения «Банк».
 * Producer, consumer и конфигурация топика ссылаются на одни и те же константы.
 */
public final class KafkaTopics {

    /** Топик событий-уведомлений. */
    public static final String NOTIFICATIONS = "notifications";

    private KafkaTopics() {
    }
}
