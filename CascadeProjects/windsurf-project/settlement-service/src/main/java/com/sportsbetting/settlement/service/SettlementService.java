package com.sportsbetting.settlement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsbetting.common.constants.Topics;
import com.sportsbetting.common.dto.EventOutcome;
import com.sportsbetting.common.dto.SettlementEvent;
import com.sportsbetting.common.enums.SettlementStatus;
import com.sportsbetting.settlement.domain.BetEntity;
import com.sportsbetting.settlement.domain.SettlementOutbox;
import com.sportsbetting.settlement.repository.BetRepository;
import com.sportsbetting.settlement.repository.SettlementOutboxRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

  private final BetRepository betRepository;
  private final SettlementOutboxRepository settlementOutboxRepository;
  private final ObjectMapper objectMapper;

  @Value("${app.settlement.payout-multiplier:2.0}")
  private Double payoutMultiplier = 2.0;

  @Transactional
  public void processEventOutcome(EventOutcome eventOutcome) {
    log.info("Processing event outcome for settlement: {}", eventOutcome);

    List<BetEntity> allBets = betRepository.findByEventId(eventOutcome.eventId());
    List<BetEntity> winningBets =
        betRepository.findWinningBets(eventOutcome.eventId(), eventOutcome.winnerId());

    log.info(
        "Found {} total bets and {} winning bets for event {}",
        allBets.size(),
        winningBets.size(),
        eventOutcome.eventId());

    for (BetEntity winningBet : winningBets) {
      createSettlementEvent(
          winningBet.toDto(),
          SettlementStatus.WON,
          calculatePayout(winningBet.getBetAmount()),
          eventOutcome.eventId());
    }

    List<BetEntity> losingBets =
        allBets.stream().filter(bet -> !winningBets.contains(bet)).toList();

    for (BetEntity losingBet : losingBets) {
      createSettlementEvent(
          losingBet.toDto(), SettlementStatus.LOST, BigDecimal.ZERO, eventOutcome.eventId());
    }

    log.info("Completed settlement processing for event: {}", eventOutcome.eventId());
  }

  private void createSettlementEvent(
      com.sportsbetting.common.dto.Bet bet,
      SettlementStatus status,
      BigDecimal payoutAmount,
      String eventId) {
    try {
      SettlementEvent settlementEvent =
          new SettlementEvent(bet.betId(), bet.userId(), bet.eventId(), status, payoutAmount);

      String payload = objectMapper.writeValueAsString(settlementEvent);
      String settlementId = generateSettlementId(bet.betId(), eventId);

      if (settlementOutboxRepository.existsBySettlementId(settlementId)) {
        log.warn("Settlement with ID {} already exists, skipping duplicate", settlementId);
        return;
      }

      SettlementOutbox outbox = new SettlementOutbox(settlementId, Topics.BET_SETTLEMENTS, payload);
      settlementOutboxRepository.save(outbox);

      log.info(
          "Created settlement event for bet {}: status={}, payout={}",
          bet.betId(),
          status,
          payoutAmount);

    } catch (JsonProcessingException e) {
      log.error("Error serializing settlement event for bet: {}", bet.betId(), e);
      throw new RuntimeException("Failed to serialize settlement event", e);
    }
  }

  private BigDecimal calculatePayout(BigDecimal betAmount) {
    return betAmount.multiply(BigDecimal.valueOf(payoutMultiplier));
  }

  private String generateSettlementId(String betId, String eventId) {
    return "SETTLE_" + betId + "_" + eventId;
  }
}
