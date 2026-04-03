package com.sportsbetting.eventapi.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsbetting.common.dto.EventOutcome;
import com.sportsbetting.eventapi.domain.OutboxEvent;
import com.sportsbetting.eventapi.domain.OutboxStatus;
import com.sportsbetting.eventapi.repository.OutboxEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

  private final OutboxEventRepository outboxEventRepository;
  private final KafkaTemplate<String, EventOutcome> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedDelayString = "${app.outbox.poll-interval}")
  @Transactional
  public void publishPendingEvents() {
    List<OutboxEvent> pendingEvents =
        outboxEventRepository.findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
            OutboxStatus.PENDING, 3);

    log.debug("Found {} pending events to publish", pendingEvents.size());

    for (OutboxEvent event : pendingEvents) {
      try {
        publishEvent(event);
        markAsPublished(event);
      } catch (Exception e) {
        log.error("Failed to publish event: {}", event, e);
        markAsFailed(event);
      }
    }
  }

  private void publishEvent(OutboxEvent event)
      throws JsonProcessingException, InterruptedException, ExecutionException {
    log.info("Publishing event to topic {}: {}", event.getTopic(), event.getEventId());

    try {
      EventOutcome eventOutcome = objectMapper.readValue(event.getPayload(), EventOutcome.class);
      kafkaTemplate.send(event.getTopic(), event.getEventId(), eventOutcome).get();
      log.info("Successfully published event: {}", event.getEventId());
    } catch (InterruptedException | ExecutionException e) {
      log.error("Failed to publish event: {}", event.getEventId(), e);
      throw e;
    }
  }

  private void markAsPublished(OutboxEvent event) {
    event.setStatus(OutboxStatus.PUBLISHED);
    event.setProcessedAt(LocalDateTime.now());
    outboxEventRepository.save(event);
    log.info("Marked event as published: {}", event.getEventId());
  }

  private void markAsFailed(OutboxEvent event) {
    event.incrementRetryCount();
    if (event.getRetryCount() >= 3) {
      event.setStatus(OutboxStatus.FAILED);
      event.setProcessedAt(LocalDateTime.now());
      log.error("Event marked as failed after 3 retries: {}", event.getEventId());
    } else {
      log.warn("Event publish failed, retry count: {}", event.getRetryCount());
    }
    outboxEventRepository.save(event);
  }
}
