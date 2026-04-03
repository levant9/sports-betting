package com.sportsbetting.settlement.messaging;

import com.sportsbetting.common.dto.EventOutcome;
import com.sportsbetting.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumer {

  private final SettlementService settlementService;

  @KafkaListener(
      topics = "${app.topics.event-outcomes}",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "kafkaListenerContainerFactory")
  public void handleEventOutcome(
      @Payload EventOutcome eventOutcome,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      Acknowledgment acknowledgment) {

    log.info("Received event outcome from topic {}: {}", topic, eventOutcome);
    log.debug("Partition: {}, Offset: {}", partition, offset);

    try {
      settlementService.processEventOutcome(eventOutcome);
      log.info("Successfully processed event outcome: {}", eventOutcome.eventId());
      acknowledgment.acknowledge();

    } catch (Exception e) {
      log.error("Error processing event outcome: {}", eventOutcome, e);
      throw e;
    }
  }
}
