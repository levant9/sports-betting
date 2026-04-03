package com.sportsbetting.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sportsbetting.common.enums.SettlementStatus;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementEvent(
    @NotBlank(message = "Bet ID is required") @JsonProperty("betId") String betId,
    @NotBlank(message = "User ID is required") @JsonProperty("userId") String userId,
    @NotBlank(message = "Event ID is required") @JsonProperty("eventId") String eventId,
    @JsonProperty("status") SettlementStatus status,
    @JsonProperty("payoutAmount") BigDecimal payoutAmount,
    LocalDateTime timestamp) {

  public SettlementEvent(
      String betId,
      String userId,
      String eventId,
      SettlementStatus status,
      BigDecimal payoutAmount,
      LocalDateTime timestamp) {
    this.betId = betId;
    this.userId = userId;
    this.eventId = eventId;
    this.status = status;
    this.payoutAmount = payoutAmount;
    this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
  }

  public SettlementEvent(
      String betId,
      String userId,
      String eventId,
      SettlementStatus status,
      BigDecimal payoutAmount) {
    this(betId, userId, eventId, status, payoutAmount, LocalDateTime.now());
  }
}
