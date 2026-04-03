package com.sportsbetting.eventapi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "event_id", nullable = false, unique = true)
  private String eventId;

  @Column(name = "topic", nullable = false)
  private String topic;

  @Column(name = "payload", columnDefinition = "CLOB")
  private String payload;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private OutboxStatus status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "processed_at")
  private LocalDateTime processedAt;

  @Column(name = "retry_count")
  private Integer retryCount = 0;

  public OutboxEvent() {
    this.createdAt = LocalDateTime.now();
    this.status = OutboxStatus.PENDING;
  }

  public OutboxEvent(String eventId, String topic, String payload) {
    this();
    this.eventId = eventId;
    this.topic = topic;
    this.payload = payload;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public OutboxStatus getStatus() {
    return status;
  }

  public void setStatus(OutboxStatus status) {
    this.status = status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getProcessedAt() {
    return processedAt;
  }

  public void setProcessedAt(LocalDateTime processedAt) {
    this.processedAt = processedAt;
  }

  public Integer getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(Integer retryCount) {
    this.retryCount = retryCount;
  }

  public void incrementRetryCount() {
    this.retryCount = (this.retryCount == null) ? 1 : this.retryCount + 1;
  }

  @Override
  public String toString() {
    return "OutboxEvent{"
        + "id="
        + id
        + ", eventId='"
        + eventId
        + '\''
        + ", topic='"
        + topic
        + '\''
        + ", status="
        + status
        + ", createdAt="
        + createdAt
        + ", processedAt="
        + processedAt
        + ", retryCount="
        + retryCount
        + '}';
  }
}
