package com.sportsbetting.eventapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsbetting.common.constants.Topics;
import com.sportsbetting.common.dto.EventOutcome;
import com.sportsbetting.eventapi.domain.OutboxEvent;
import com.sportsbetting.eventapi.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

  private final OutboxEventRepository outboxEventRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  public void saveEventOutcome(EventOutcome eventOutcome) {
    log.info("Saving event outcome to outbox: {}", eventOutcome);

    try {
      String payload = objectMapper.writeValueAsString(eventOutcome);
      String eventId = generateEventId(eventOutcome);

      if (outboxEventRepository.existsByEventId(eventId)) {
        log.warn("Event with ID {} already exists, skipping duplicate", eventId);
        return;
      }

      OutboxEvent outboxEvent = new OutboxEvent(eventId, Topics.EVENT_OUTCOMES, payload);
      outboxEventRepository.save(outboxEvent);

      log.info("Successfully saved event outcome to outbox with ID: {}", eventId);

    } catch (JsonProcessingException e) {
      log.error("Error serializing event outcome: {}", eventOutcome, e);
      throw new RuntimeException("Failed to serialize event outcome", e);
    } catch (DataIntegrityViolationException e) {
      log.warn("Duplicate event detected, ignoring: {}", eventOutcome.eventId());
    }
  }

  private String generateEventId(EventOutcome eventOutcome) {
    return eventOutcome.eventId() + "_" + System.currentTimeMillis();
  }
}
