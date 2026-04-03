package com.sportsbetting.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class EventOutcomeTest {

  @Test
  void shouldCreateEventOutcomeWithAllFields() {
    EventOutcome eventOutcome = new EventOutcome("E1", "Match", "T1");

    assertThat(eventOutcome.eventId()).isEqualTo("E1");
    assertThat(eventOutcome.eventName()).isEqualTo("Match");
    assertThat(eventOutcome.winnerId()).isEqualTo("T1");
    assertThat(eventOutcome.timestamp()).isNotNull();
  }

  @Test
  void shouldCreateEventOutcomeWithTimestamp() {
    LocalDateTime customTime = LocalDateTime.of(2024, 1, 1, 12, 0);
    EventOutcome eventOutcome = new EventOutcome("E2", "Final", "T2", customTime);

    assertThat(eventOutcome.eventId()).isEqualTo("E2");
    assertThat(eventOutcome.eventName()).isEqualTo("Final");
    assertThat(eventOutcome.winnerId()).isEqualTo("T2");
    assertThat(eventOutcome.timestamp()).isEqualTo(customTime);
  }

  @Test
  void shouldHaveCorrectToString() {
    EventOutcome eventOutcome = new EventOutcome("E3", "Semi", "T3");

    String toString = eventOutcome.toString();
    assertThat(toString).contains("E3");
    assertThat(toString).contains("Semi");
    assertThat(toString).contains("T3");
  }

  @Test
  void shouldImplementEqualsAndHashCode() {
    EventOutcome event1 = new EventOutcome("E1", "Match", "T1");
    EventOutcome event2 = new EventOutcome("E1", "Match", "T1");
    EventOutcome event3 = new EventOutcome("E2", "Match", "T1");

    assertThat(event1).isEqualTo(event2);
    assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    assertThat(event1).isNotEqualTo(event3);
  }
}
