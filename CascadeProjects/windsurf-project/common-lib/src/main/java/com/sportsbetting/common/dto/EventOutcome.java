package com.sportsbetting.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record EventOutcome(
    @NotBlank(message = "Event ID is required") @JsonProperty("eventId") String eventId,
    @NotBlank(message = "Event name is required") @JsonProperty("eventName") String eventName,
    @NotBlank(message = "Winner ID is required") @JsonProperty("winnerId") String winnerId,
    @JsonProperty("timestamp") LocalDateTime timestamp) {

  @JsonCreator
  public EventOutcome(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("eventName") String eventName,
      @JsonProperty("winnerId") String winnerId,
      @JsonProperty("timestamp") LocalDateTime timestamp) {
    this.eventId = eventId;
    this.eventName = eventName;
    this.winnerId = winnerId;
    this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
  }

  public EventOutcome(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("eventName") String eventName,
      @JsonProperty("winnerId") String winnerId) {
    this(eventId, eventName, winnerId, LocalDateTime.now());
  }
}
