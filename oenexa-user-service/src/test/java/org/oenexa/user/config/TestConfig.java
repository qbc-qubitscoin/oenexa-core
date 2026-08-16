package org.oenexa.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Test-only Spring configuration for the User Service.
 *
 * <p>Provides:
 * <ul>
 *   <li>A primary {@link ObjectMapper} with dates serialised as ISO strings (not timestamps).</li>
 *   <li>An in-process Kafka listener container factory with {@code autoStartup=false},
 *       preventing real broker connections during the test run while still allowing the
 *       {@link org.oenexa.user.kafka.consumer.UserEventConsumer} bean to be instantiated and
 *       its methods invoked directly in unit tests.</li>
 * </ul>
 */
@TestConfiguration
public class TestConfig {

    /** Ephemeral bootstrap URL — must match {@code application-test.yml}. */
    private static final String TEST_BROKER = "localhost:9999";

    /**
     * Provides a primary {@link ObjectMapper} that serialises {@code LocalDate} /
     * {@code LocalDateTime} as ISO-8601 strings rather than numeric timestamps.
     * Spring Boot's auto-configured {@code JavaTimeModule} registration is still active;
     * this bean overrides the feature flag only.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Provides a {@link ConcurrentKafkaListenerContainerFactory} pointed at a non-existent
     * test broker with {@code autoStartup=false}, preventing real Kafka connections while
     * still allowing listener beans to be constructed.
     */
    @Bean
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TEST_BROKER);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerProps));
        factory.setAutoStartup(false);
        return factory;
    }

    /**
     * Provides a {@link KafkaTemplate} for direct message sending in future integration tests.
     */
    @Bean
    @Primary
    public KafkaTemplate<String, String> kafkaTemplate() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, TEST_BROKER);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));
    }
}
