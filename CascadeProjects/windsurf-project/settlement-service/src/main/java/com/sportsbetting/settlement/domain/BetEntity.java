package com.sportsbetting.settlement.domain;

import com.sportsbetting.common.dto.Bet;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bets")
public class BetEntity {

  @Id
  @NotBlank(message = "Bet ID is required")
  @Column(name = "bet_id")
  private String betId;

  @NotBlank(message = "User ID is required")
  @Column(name = "user_id")
  private String userId;

  @NotBlank(message = "Event ID is required")
  @Column(name = "event_id")
  private String eventId;

  @NotBlank(message = "Event market ID is required")
  @Column(name = "event_market_id")
  private String eventMarketId;

  @NotBlank(message = "Event winner ID is required")
  @Column(name = "event_winner_id")
  private String eventWinnerId;

  @NotNull(message = "Bet amount is required")
  @Positive(message = "Bet amount must be positive")
  @Column(name = "bet_amount", precision = 10, scale = 2)
  private BigDecimal betAmount;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  public BetEntity() {
    this.createdAt = LocalDateTime.now();
  }

  public BetEntity(
      String betId,
      String userId,
      String eventId,
      String eventMarketId,
      String eventWinnerId,
      BigDecimal betAmount) {
    this();
    this.betId = betId;
    this.userId = userId;
    this.eventId = eventId;
    this.eventMarketId = eventMarketId;
    this.eventWinnerId = eventWinnerId;
    this.betAmount = betAmount;
  }

  public BetEntity(Bet bet) {
    this();
    this.betId = bet.betId();
    this.userId = bet.userId();
    this.eventId = bet.eventId();
    this.eventMarketId = bet.eventMarketId();
    this.eventWinnerId = bet.eventWinnerId();
    this.betAmount = bet.betAmount();
    this.createdAt = bet.createdAt();
  }

  public Bet toDto() {
    return new Bet(
        this.betId,
        this.userId,
        this.eventId,
        this.eventMarketId,
        this.eventWinnerId,
        this.betAmount,
        this.createdAt);
  }

  public String getBetId() {
    return betId;
  }

  public void setBetId(String betId) {
    this.betId = betId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String getEventMarketId() {
    return eventMarketId;
  }

  public void setEventMarketId(String eventMarketId) {
    this.eventMarketId = eventMarketId;
  }

  public String getEventWinnerId() {
    return eventWinnerId;
  }

  public void setEventWinnerId(String eventWinnerId) {
    this.eventWinnerId = eventWinnerId;
  }

  public BigDecimal getBetAmount() {
    return betAmount;
  }

  public void setBetAmount(BigDecimal betAmount) {
    this.betAmount = betAmount;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public String toString() {
    return "BetEntity{"
        + "betId='"
        + betId
        + '\''
        + ", userId='"
        + userId
        + '\''
        + ", eventId='"
        + eventId
        + '\''
        + ", eventMarketId='"
        + eventMarketId
        + '\''
        + ", eventWinnerId='"
        + eventWinnerId
        + '\''
        + ", betAmount="
        + betAmount
        + ", createdAt="
        + createdAt
        + '}';
  }
}
