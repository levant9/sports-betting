package com.sportsbetting.eventapi.repository;

import com.sportsbetting.eventapi.domain.OutboxEvent;
import com.sportsbetting.eventapi.domain.OutboxStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

  @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status ORDER BY o.createdAt ASC")
  List<OutboxEvent> findByStatusOrderByCreatedAtAsc(@Param("status") OutboxStatus status);

  @Query(
      "SELECT o FROM OutboxEvent o WHERE o.status = :status AND o.retryCount < :maxRetries ORDER BY o.createdAt ASC")
  List<OutboxEvent> findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
      @Param("status") OutboxStatus status, @Param("maxRetries") Integer maxRetries);

  boolean existsByEventId(String eventId);
}
