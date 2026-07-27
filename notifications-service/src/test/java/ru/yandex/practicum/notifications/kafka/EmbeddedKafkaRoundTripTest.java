package ru.yandex.practicum.notifications.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.shared.kafka.KafkaTopics;
import ru.yandex.practicum.shared.kafka.dto.NotificationEvent;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Интеграционный тест сериализации NotificationEvent и round-trip через Kafka
 * (in-JVM брокер EmbeddedKafka, без Docker).
 */
@SpringJUnitConfig(EmbeddedKafkaRoundTripTest.TestConfig.class)
@EmbeddedKafka(partitions = 1, topics = KafkaTopics.NOTIFICATIONS)
class EmbeddedKafkaRoundTripTest {

    @Configuration
    static class TestConfig {
    }

    @Autowired
    private EmbeddedKafkaBroker broker;

    @Test
    void notificationEventShouldRoundTripThroughKafka() {
        NotificationEvent event = new NotificationEvent("user", "hello", "ACCOUNT_CREATED");

        Map<String, Object> producerProps = new HashMap<>(KafkaTestUtils.producerProps(broker));
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        try (Producer<String, NotificationEvent> producer =
                     new DefaultKafkaProducerFactory<String, NotificationEvent>(producerProps).createProducer()) {
            producer.send(new ProducerRecord<>(KafkaTopics.NOTIFICATIONS, event.userLogin(), event));
            producer.flush();
        }

        Map<String, Object> consumerProps = new HashMap<>(KafkaTestUtils.consumerProps("round-trip-group", "true", broker));
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NotificationEvent.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (Consumer<String, NotificationEvent> consumer =
                     new DefaultKafkaConsumerFactory<String, NotificationEvent>(consumerProps).createConsumer()) {
            consumer.subscribe(List.of(KafkaTopics.NOTIFICATIONS));
            ConsumerRecords<String, NotificationEvent> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));

            assertEquals(1, records.count());
            ConsumerRecord<String, NotificationEvent> received = records.iterator().next();
            assertEquals("user", received.key());
            assertEquals("user", received.value().userLogin());
            assertEquals("hello", received.value().message());
            assertEquals("ACCOUNT_CREATED", received.value().type());
        }
    }
}
