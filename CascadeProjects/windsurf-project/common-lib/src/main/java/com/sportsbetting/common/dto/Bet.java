package com.sportsbetting.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Bet(
    @NotBlank(message = "Bet ID is required") @JsonProperty("betId") String betId,
    @NotBlank(message = "User ID is required") @JsonProperty("userId") String userId,
    @NotBlank(message = "Event ID is required") @JsonProperty("eventId") String eventId,
    @NotBlank(message = "Event market ID is required") @JsonProperty("eventMarketId")
        String eventMarketId,
    @NotBlank(message = "Event winner ID is required") @JsonProperty("eventWinnerId")
        String eventWinnerId,
    @NotNull(message = "Bet amount is required")
        @Positive(message = "Bet amount must be positive")
        @JsonProperty("betAmount")
        BigDecimal betAmount,
    @JsonProperty("createdAt") LocalDateTime createdAt) {

  public Bet(
      String betId,
      String userId,
      String eventId,
      String eventMarketId,
      String eventWinnerId,
      BigDecimal betAmount,
      LocalDateTime createdAt) {
    this.betId = betId;
    this.userId = userId;
    this.eventId = eventId;
    this.eventMarketId = eventMarketId;
    this.eventWinnerId = eventWinnerId;
    this.betAmount = betAmount;
    this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
  }

  public Bet(
      String betId,
      String userId,
      String eventId,
      String eventMarketId,
      String eventWinnerId,
      BigDecimal betAmount) {
    this(betId, userId, eventId, eventMarketId, eventWinnerId, betAmount, LocalDateTime.now());
  }
}
