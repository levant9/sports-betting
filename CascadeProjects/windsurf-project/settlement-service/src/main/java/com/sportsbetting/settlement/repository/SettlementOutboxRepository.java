package com.sportsbetting.settlement.repository;

import com.sportsbetting.settlement.domain.OutboxStatus;
import com.sportsbetting.settlement.domain.SettlementOutbox;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementOutboxRepository extends JpaRepository<SettlementOutbox, Long> {

  @Query("SELECT s FROM SettlementOutbox s WHERE s.status = :status ORDER BY s.createdAt ASC")
  List<SettlementOutbox> findByStatusOrderByCreatedAtAsc(@Param("status") OutboxStatus status);

  @Query(
      "SELECT s FROM SettlementOutbox s WHERE s.status = :status AND s.retryCount < :maxRetries ORDER BY s.createdAt ASC")
  List<SettlementOutbox> findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
      @Param("status") OutboxStatus status, @Param("maxRetries") Integer maxRetries);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT s FROM SettlementOutbox s WHERE s.id = :id")
  Optional<SettlementOutbox> findByIdWithLock(@Param("id") Long id);

  @Modifying
  @Query(
      "UPDATE SettlementOutbox s SET s.status = 'PROCESSING' WHERE s.id = :id AND s.status = 'PENDING'")
  int markAsProcessing(@Param("id") Long id);

  boolean existsBySettlementId(String settlementId);
}
