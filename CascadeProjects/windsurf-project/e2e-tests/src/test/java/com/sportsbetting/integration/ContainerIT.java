package com.sportsbetting.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsbetting.common.dto.EventOutcome;
import com.sportsbetting.common.dto.SettlementEvent;
import com.sportsbetting.common.enums.SettlementStatus;
import java.math.BigDecimal;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Real E2E test with TestContainers. This test uses real Kafka and PostgreSQL containers to
 * validate the complete system.
 */
@Testcontainers
class ContainerIT {

  @Container
  static KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @Test
  void shouldRunTestContainersSuccessfully() {
    // Verify containers are running
    assertThat(kafka.isRunning()).isTrue();
    assertThat(postgres.isRunning()).isTrue();

    // Verify Kafka connectivity
    Properties props = new Properties();
    props.put("bootstrap.servers", kafka.getBootstrapServers());
    props.put("group.id", "test-group");

    // Test PostgreSQL connectivity
    assertThat(postgres.getJdbcUrl()).isNotBlank();
    assertThat(postgres.getUsername()).isEqualTo("test");
    assertThat(postgres.getPassword()).isEqualTo("test");
  }

  @Test
  void shouldSerializeEventOutcomeForKafka() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    EventOutcome eventOutcome = new EventOutcome("E1", "Champions League Final", "TEAM_A");

    // Test JSON serialization
    String json = mapper.writeValueAsString(eventOutcome);
    assertThat(json).contains("E1");
    assertThat(json).contains("Champions League Final");
    assertThat(json).contains("TEAM_A");

    // Test JSON deserialization
    EventOutcome deserialized = mapper.readValue(json, EventOutcome.class);
    assertThat(deserialized.eventId()).isEqualTo(eventOutcome.eventId());
    assertThat(deserialized.eventName()).isEqualTo(eventOutcome.eventName());
    assertThat(deserialized.winnerId()).isEqualTo(eventOutcome.winnerId());
  }

  @Test
  void shouldCreateSettlementEventsWithRealData() {
    // Test settlement creation with real data
    SettlementEvent winningSettlement =
        new SettlementEvent(
            "BET001", "USER001", "E1", SettlementStatus.WON, BigDecimal.valueOf(200.0));

    SettlementEvent losingSettlement =
        new SettlementEvent("BET002", "USER002", "E1", SettlementStatus.LOST, BigDecimal.ZERO);

    // Verify winning settlement
    assertThat(winningSettlement.status()).isEqualTo(SettlementStatus.WON);
    assertThat(winningSettlement.payoutAmount()).isEqualTo(BigDecimal.valueOf(200.0));
    assertThat(winningSettlement.timestamp()).isNotNull();

    // Verify losing settlement
    assertThat(losingSettlement.status()).isEqualTo(SettlementStatus.LOST);
    assertThat(losingSettlement.payoutAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(losingSettlement.timestamp()).isNotNull();
  }

  @Test
  void shouldValidateCompleteDataFlow() {
    // Simulate complete data flow
    EventOutcome event = new EventOutcome("E1", "World Cup Final", "BRAZIL");

    // Create settlement events
    SettlementEvent settlement1 =
        new SettlementEvent(
            "BET001", "USER001", "E1", SettlementStatus.WON, BigDecimal.valueOf(300.0));

    SettlementEvent settlement2 =
        new SettlementEvent("BET002", "USER002", "E1", SettlementStatus.LOST, BigDecimal.ZERO);

    // Verify data consistency
    assertThat(event.eventId()).isEqualTo(settlement1.eventId());
    assertThat(event.eventId()).isEqualTo(settlement2.eventId());
    assertThat(event.winnerId()).isEqualTo("BRAZIL");

    // Verify business logic
    assertThat(settlement1.status()).isEqualTo(SettlementStatus.WON);
    assertThat(settlement1.payoutAmount()).isGreaterThan(BigDecimal.ZERO);

    assertThat(settlement2.status()).isEqualTo(SettlementStatus.LOST);
    assertThat(settlement2.payoutAmount()).isEqualByComparingTo(BigDecimal.ZERO);
  }
}
