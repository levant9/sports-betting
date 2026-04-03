package com.sportsbetting.settlement.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsbetting.settlement.domain.OutboxStatus;
import com.sportsbetting.settlement.domain.SettlementOutbox;
import com.sportsbetting.settlement.repository.SettlementOutboxRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RocketMQPublisher {

  private final SettlementOutboxRepository settlementOutboxRepository;
  private final RocketMQTemplate rocketMQTemplate;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedDelayString = "${app.outbox.poll-interval}")
  @Transactional
  public void publishPendingSettlements() {
    List<SettlementOutbox> pendingSettlements =
        settlementOutboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

    log.debug("Found {} pending settlements to publish", pendingSettlements.size());

    for (SettlementOutbox settlement : pendingSettlements) {
      try {
        // Mark as processing first to prevent other instances from picking it up
        int updated = settlementOutboxRepository.markAsProcessing(settlement.getId());
        if (updated == 0) {
          // Another instance already processing this settlement
          log.debug(
              "Settlement {} already being processed by another instance",
              settlement.getSettlementId());
          continue;
        }

        publishSettlement(settlement);
        markAsPublished(settlement);
      } catch (Exception e) {
        log.error("Failed to publish settlement: {}", settlement, e);
        markAsFailed(settlement);
      }
    }
  }

  private void publishSettlement(SettlementOutbox settlement) throws JsonProcessingException {
    log.info(
        "Publishing settlement to topic {}: {}",
        settlement.getTopic(),
        settlement.getSettlementId());

    SendResult sendResult =
        rocketMQTemplate.syncSend(settlement.getTopic(), settlement.getPayload());

    log.info(
        "Successfully published settlement: {} with result: {}",
        settlement.getSettlementId(),
        sendResult.getSendStatus());
  }

  private void markAsPublished(SettlementOutbox settlement) {
    settlement.setStatus(OutboxStatus.PUBLISHED);
    settlement.setProcessedAt(LocalDateTime.now());
    settlementOutboxRepository.save(settlement);
    log.info("Marked settlement as published: {}", settlement.getSettlementId());
  }

  private void markAsFailed(SettlementOutbox settlement) {
    settlement.incrementRetryCount();
    if (settlement.getRetryCount() >= 3) {
      settlement.setStatus(OutboxStatus.FAILED);
      settlement.setProcessedAt(LocalDateTime.now());
      log.error("Settlement marked as failed after 3 retries: {}", settlement.getSettlementId());
    } else {
      log.warn("Settlement publish failed, retry count: {}", settlement.getRetryCount());
    }
    settlementOutboxRepository.save(settlement);
  }
}
