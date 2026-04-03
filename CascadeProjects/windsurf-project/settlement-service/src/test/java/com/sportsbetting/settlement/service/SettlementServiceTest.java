package com.sportsbetting.settlement.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsbetting.common.dto.EventOutcome;
import com.sportsbetting.settlement.domain.BetEntity;
import com.sportsbetting.settlement.domain.SettlementOutbox;
import com.sportsbetting.settlement.repository.BetRepository;
import com.sportsbetting.settlement.repository.SettlementOutboxRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

  @Mock private BetRepository betRepository;

  @Mock private SettlementOutboxRepository settlementOutboxRepository;

  @Mock private ObjectMapper objectMapper;

  @InjectMocks private SettlementService settlementService;

  private EventOutcome eventOutcome;
  private List<BetEntity> allBets;
  private List<BetEntity> winningBets;

  @BeforeEach
  void setUp() {
    eventOutcome = new EventOutcome("E1", "Match", "T1");

    BetEntity winningBet1 =
        new BetEntity("BET001", "USER001", "E1", "MARKET1", "T1", new BigDecimal("100.00"));
    BetEntity winningBet2 =
        new BetEntity("BET003", "USER003", "E1", "MARKET1", "T1", new BigDecimal("75.00"));
    BetEntity losingBet =
        new BetEntity("BET002", "USER002", "E1", "MARKET1", "T2", new BigDecimal("50.00"));

    allBets = List.of(winningBet1, losingBet, winningBet2);
    winningBets = List.of(winningBet1, winningBet2);
  }

  @Test
  void shouldProcessEventOutcomeAndCreateSettlements() throws Exception {
    String expectedPayload =
        "{\"betId\":\"BET001\",\"userId\":\"USER001\",\"eventId\":\"E1\",\"status\":\"WON\",\"payoutAmount\":200.00}";

    when(betRepository.findByEventId("E1")).thenReturn(allBets);
    when(betRepository.findWinningBets("E1", "T1")).thenReturn(winningBets);
    when(settlementOutboxRepository.existsBySettlementId(anyString())).thenReturn(false);
    when(objectMapper.writeValueAsString(any())).thenReturn(expectedPayload);

    settlementService.processEventOutcome(eventOutcome);

    verify(betRepository).findByEventId("E1");
    verify(betRepository).findWinningBets("E1", "T1");
    verify(settlementOutboxRepository, times(3)).save(any(SettlementOutbox.class));
  }

  @Test
  void shouldSkipDuplicateSettlements() {
    when(betRepository.findByEventId("E1")).thenReturn(allBets);
    when(betRepository.findWinningBets("E1", "T1")).thenReturn(winningBets);
    when(settlementOutboxRepository.existsBySettlementId(anyString())).thenReturn(true);

    settlementService.processEventOutcome(eventOutcome);

    verify(settlementOutboxRepository, never()).save(any(SettlementOutbox.class));
  }

  @Test
  void shouldCalculateCorrectPayout() {
    when(betRepository.findByEventId("E1")).thenReturn(allBets);
    when(betRepository.findWinningBets("E1", "T1")).thenReturn(winningBets);
    when(settlementOutboxRepository.existsBySettlementId(anyString())).thenReturn(false);

    settlementService.processEventOutcome(eventOutcome);

    verify(settlementOutboxRepository, times(3)).save(any(SettlementOutbox.class));
  }
}
