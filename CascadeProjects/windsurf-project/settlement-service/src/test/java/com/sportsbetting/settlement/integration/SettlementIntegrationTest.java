package com.sportsbetting.settlement.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsbetting.common.dto.EventOutcome;
import com.sportsbetting.settlement.domain.OutboxStatus;
import com.sportsbetting.settlement.domain.SettlementOutbox;
import com.sportsbetting.settlement.repository.SettlementOutboxRepository;
import com.sportsbetting.settlement.service.SettlementService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@DirtiesContext
class SettlementIntegrationTest {

  @Autowired private SettlementService settlementService;

  @Autowired private SettlementOutboxRepository settlementOutboxRepository;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    settlementOutboxRepository.deleteAll();
  }

  @Test
  void shouldProcessEventOutcomeAndCreateSettlements() {
    EventOutcome eventOutcome = new EventOutcome("E1", "Match", "T1");

    settlementService.processEventOutcome(eventOutcome);

    List<SettlementOutbox> settlements = settlementOutboxRepository.findAll();
    assertThat(settlements).hasSize(3);

    settlements.forEach(
        settlement -> {
          assertThat(settlement.getTopic()).isEqualTo("bet-settlements");
          assertThat(settlement.getStatus()).isEqualTo(OutboxStatus.PENDING);
          assertThat(settlement.getSettlementId()).contains("SETTLE_");
        });
  }

  @Test
  void shouldProcessDuplicateEvents() {
    // Clean up before test
    settlementOutboxRepository.deleteAll();

    EventOutcome eventOutcome = new EventOutcome("E2", "Final", "T2");

    settlementService.processEventOutcome(eventOutcome);
    settlementService.processEventOutcome(eventOutcome);

    List<SettlementOutbox> settlements = settlementOutboxRepository.findAll();
    // First call creates 2 settlements (2 losing bets: BET004, BET005)
    // Second call creates 0 settlements (idempotency prevents duplicates)
    assertThat(settlements).hasSize(2);
  }
}
