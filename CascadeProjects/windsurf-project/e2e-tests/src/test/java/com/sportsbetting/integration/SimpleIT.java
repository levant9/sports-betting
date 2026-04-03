package com.sportsbetting.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.sportsbetting.common.dto.Bet;
import com.sportsbetting.common.dto.EventOutcome;
import com.sportsbetting.common.dto.SettlementEvent;
import com.sportsbetting.common.enums.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * Simple integration test that validates the core DTOs and enums work together. This test runs
 * quickly and validates basic integration without external dependencies.
 */
class SimpleIT {

  @Test
  void shouldIntegrateEventOutcomeAndBets() {
    // Create event outcome
    EventOutcome event = new EventOutcome("E1", "Champions League", "TEAM_A");

    // Create bets for the event
    Bet winningBet =
        new Bet(
            "BET001",
            "USER001",
            "E1",
            "WINNER",
            "TEAM_A",
            BigDecimal.valueOf(100.0),
            LocalDateTime.now());
    Bet losingBet =
        new Bet(
            "BET002",
            "USER002",
            "E1",
            "WINNER",
            "TEAM_B",
            BigDecimal.valueOf(50.0),
            LocalDateTime.now());

    // Verify relationships
    assertThat(winningBet.eventId()).isEqualTo(event.eventId());
    assertThat(losingBet.eventId()).isEqualTo(event.eventId());
    assertThat(winningBet.eventWinnerId()).isEqualTo(event.winnerId());
    assertThat(losingBet.eventWinnerId()).isNotEqualTo(event.winnerId());
  }

  @Test
  void shouldCreateSettlementEventsCorrectly() {
    // Test winning settlement
    SettlementEvent winningSettlement =
        new SettlementEvent(
            "BET001", "USER001", "E1", SettlementStatus.WON, BigDecimal.valueOf(200.0));

    // Test losing settlement
    SettlementEvent losingSettlement =
        new SettlementEvent("BET002", "USER002", "E1", SettlementStatus.LOST, BigDecimal.ZERO);

    // Verify settlement properties
    assertThat(winningSettlement.status()).isEqualTo(SettlementStatus.WON);
    assertThat(winningSettlement.payoutAmount()).isPositive();
    assertThat(winningSettlement.timestamp()).isNotNull();

    assertThat(losingSettlement.status()).isEqualTo(SettlementStatus.LOST);
    assertThat(losingSettlement.payoutAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(losingSettlement.timestamp()).isNotNull();
  }

  @Test
  void shouldValidateSettlementStatusEnum() {
    // Test enum values
    assertThat(SettlementStatus.values()).hasSize(2);
    assertThat(SettlementStatus.WON).isNotNull();
    assertThat(SettlementStatus.LOST).isNotNull();

    // Test enum consistency
    assertThat(SettlementStatus.valueOf("WON")).isEqualTo(SettlementStatus.WON);
    assertThat(SettlementStatus.valueOf("LOST")).isEqualTo(SettlementStatus.LOST);
  }
}
