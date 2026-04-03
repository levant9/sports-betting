package com.sportsbetting.eventapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsbetting.common.dto.EventOutcome;
import com.sportsbetting.eventapi.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventController.class)
class EventControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private EventService eventService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldReturnAcceptedForValidEvent() throws Exception {
    EventOutcome eventOutcome = new EventOutcome("E1", "Match", "T1");

    String json = objectMapper.writeValueAsString(eventOutcome);
    System.out.println("Generated JSON: " + json);

    doNothing().when(eventService).saveEventOutcome(any(EventOutcome.class));

    mockMvc
        .perform(post("/api/events/outcome").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isAccepted());
  }

  @Test
  void shouldReturnBadRequestForInvalidEvent() throws Exception {
    String invalidEvent = "{\"eventId\":\"\",\"eventName\":\"Match\",\"winnerId\":\"T1\"}";

    mockMvc
        .perform(
            post("/api/events/outcome")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidEvent))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnInternalServerErrorWhenServiceFails() throws Exception {
    EventOutcome eventOutcome = new EventOutcome("E1", "Match", "T1");

    doThrow(new RuntimeException("Service error"))
        .when(eventService)
        .saveEventOutcome(any(EventOutcome.class));

    mockMvc
        .perform(
            post("/api/events/outcome")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventOutcome)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void shouldReturnHealthStatus() throws Exception {
    mockMvc
        .perform(get("/api/events/health"))
        .andExpect(status().isOk())
        .andExpect(content().string("Event API Service is healthy"));
  }
}
