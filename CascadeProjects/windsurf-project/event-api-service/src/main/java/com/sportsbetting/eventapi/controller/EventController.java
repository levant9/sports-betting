package com.sportsbetting.eventapi.controller;

import com.sportsbetting.common.dto.EventOutcome;
import com.sportsbetting.eventapi.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

  private final EventService eventService;

  @PostMapping("/outcome")
  public ResponseEntity<Void> publishEventOutcome(@Valid @RequestBody EventOutcome eventOutcome) {
    log.info("Received event outcome: {}", eventOutcome);

    try {
      eventService.saveEventOutcome(eventOutcome);
      return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    } catch (Exception e) {
      log.error("Error processing event outcome: {}", eventOutcome, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Event API Service is healthy");
  }
}
