package com.sportsbetting.eventapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsbetting.common.dto.EventOutcome;
import com.sportsbetting.eventapi.domain.OutboxEvent;
import com.sportsbetting.eventapi.domain.OutboxStatus;
import com.sportsbetting.eventapi.repository.OutboxEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@DirtiesContext
class EventApiIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private OutboxEventRepository outboxEventRepository;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldSaveEventToOutbox() {
    // Clean up before test
    outboxEventRepository.deleteAll();

    EventOutcome eventOutcome = new EventOutcome("E1", "Match", "T1");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<EventOutcome> request = new HttpEntity<>(eventOutcome, headers);

    ResponseEntity<Void> response =
        restTemplate.postForEntity("/api/events/outcome", request, Void.class);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

    await()
        .untilAsserted(
            () -> {
              List<OutboxEvent> events = outboxEventRepository.findAll();
              assertThat(events).hasSize(1);

              OutboxEvent savedEvent = events.get(0);
              assertThat(savedEvent.getTopic()).isEqualTo("event-outcomes");
              assertThat(savedEvent.getStatus()).isEqualTo(OutboxStatus.PENDING);
              assertThat(savedEvent.getEventId()).contains("E1");
              assertThat(savedEvent.getCreatedAt()).isBefore(LocalDateTime.now().plusMinutes(1));
            });
  }

  @Test
  void shouldHandleDuplicateEvents() {
    // Clean up before test
    outboxEventRepository.deleteAll();

    EventOutcome eventOutcome = new EventOutcome("E2", "Final", "T2");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<EventOutcome> request = new HttpEntity<>(eventOutcome, headers);

    ResponseEntity<Void> firstResponse =
        restTemplate.postForEntity("/api/events/outcome", request, Void.class);

    ResponseEntity<Void> secondResponse =
        restTemplate.postForEntity("/api/events/outcome", request, Void.class);

    assertEquals(HttpStatus.ACCEPTED, firstResponse.getStatusCode());
    assertEquals(HttpStatus.ACCEPTED, secondResponse.getStatusCode());

    await()
        .untilAsserted(
            () -> {
              List<OutboxEvent> events = outboxEventRepository.findAll();
              assertThat(events).hasSize(2);
            });
  }
}
