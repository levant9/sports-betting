package com.sportsbetting.eventapi.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsbetting.common.dto.EventOutcome;
import com.sportsbetting.eventapi.domain.OutboxEvent;
import com.sportsbetting.eventapi.domain.OutboxStatus;
import com.sportsbetting.eventapi.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

  @Mock private OutboxEventRepository outboxEventRepository;

  @Mock private ObjectMapper objectMapper;

  @InjectMocks private EventService eventService;

  private EventOutcome eventOutcome;

  @BeforeEach
  void setUp() {
    eventOutcome = new EventOutcome("E1", "Match", "T1");
  }

  @Test
  void shouldSaveEventOutcomeSuccessfully() throws Exception {
    String expectedPayload = "{\"eventId\":\"E1\",\"eventName\":\"Match\",\"winnerId\":\"T1\"}";

    when(objectMapper.writeValueAsString(eventOutcome)).thenReturn(expectedPayload);
    when(outboxEventRepository.existsByEventId(anyString())).thenReturn(false);
    when(outboxEventRepository.save(any(OutboxEvent.class)))
        .thenAnswer(
            invocation -> {
              OutboxEvent event = invocation.getArgument(0);
              event.setId(1L);
              return event;
            });

    eventService.saveEventOutcome(eventOutcome);

    verify(outboxEventRepository).existsByEventId(anyString());
    verify(outboxEventRepository)
        .save(
            argThat(
                event ->
                    event.getTopic().equals("event-outcomes")
                        && event.getPayload().equals(expectedPayload)
                        && event.getStatus() == OutboxStatus.PENDING));
  }

  @Test
  void shouldSkipDuplicateEvent() {
    when(outboxEventRepository.existsByEventId(anyString())).thenReturn(true);

    eventService.saveEventOutcome(eventOutcome);

    verify(outboxEventRepository).existsByEventId(anyString());
    verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
  }

  @Test
  void shouldThrowExceptionWhenSerializationFails() throws Exception {
    when(objectMapper.writeValueAsString(eventOutcome))
        .thenThrow(new RuntimeException("Serialization failed"));

    assertThatThrownBy(() -> eventService.saveEventOutcome(eventOutcome))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Serialization failed");
  }
}
