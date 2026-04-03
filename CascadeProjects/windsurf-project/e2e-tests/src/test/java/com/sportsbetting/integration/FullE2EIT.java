package com.sportsbetting.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsbetting.common.dto.EventOutcome;
import com.sportsbetting.common.dto.SettlementEvent;
import com.sportsbetting.common.enums.SettlementStatus;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = TestApplication.class)
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@Testcontainers
@DirtiesContext
class FullE2EIT {

  @Container
  static KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

  @Container
  static GenericContainer<?> rocketmq =
      new GenericContainer<>(DockerImageName.parse("apache/rocketmq:4.9.4"))
          .withExposedPorts(9876, 10911)
          .withCommand("sh", "mqnamesrv");

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @Autowired private ObjectMapper objectMapper;

  @Autowired private EmbeddedKafkaBroker embeddedKafka;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    registry.add(
        "rocketmq.name-server", () -> rocketmq.getHost() + ":" + rocketmq.getMappedPort(9876));

    // Database properties for both services
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);

    // JPA properties
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    registry.add("spring.jpa.show-sql", () -> "true");
    registry.add(
        "spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
  }

  @Test
  void shouldProcessEventFromKafkaToRocketMQ() throws Exception {
    EventOutcome eventOutcome = new EventOutcome("E1", "Match", "T1");

    Properties producerProps = new Properties();
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

    try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
      String eventJson = objectMapper.writeValueAsString(eventOutcome);
      ProducerRecord<String, String> record =
          new ProducerRecord<>("event-outcomes", eventOutcome.eventId(), eventJson);
      producer.send(record).get();
    }

    // Wait for processing
    Thread.sleep(5000);

    Properties consumerProps = new Properties();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    try (Consumer<String, String> consumer =
        new org.apache.kafka.clients.consumer.KafkaConsumer<>(consumerProps)) {
      consumer.subscribe(Collections.singletonList("bet-settlements"));

      ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

      assertThat(records).hasSize(3);

      for (ConsumerRecord<String, String> record : records) {
        SettlementEvent settlement = objectMapper.readValue(record.value(), SettlementEvent.class);
        assertThat(settlement.eventId()).isEqualTo("E1");
        assertThat(settlement.status()).isIn(SettlementStatus.WON, SettlementStatus.LOST);

        if (settlement.status() == SettlementStatus.WON) {
          assertThat(settlement.payoutAmount()).isNotNull();
          assertThat(settlement.payoutAmount()).isPositive();
        } else {
          assertThat(settlement.payoutAmount()).isEqualByComparingTo(java.math.BigDecimal.ZERO);
        }
      }
    }
  }
}
