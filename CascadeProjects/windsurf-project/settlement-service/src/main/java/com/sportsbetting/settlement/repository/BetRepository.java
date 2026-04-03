package com.sportsbetting.settlement.repository;

import com.sportsbetting.settlement.domain.BetEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BetRepository extends JpaRepository<BetEntity, String> {

  @Query("SELECT b FROM BetEntity b WHERE b.eventId = :eventId")
  List<BetEntity> findByEventId(@Param("eventId") String eventId);

  @Query("SELECT b FROM BetEntity b WHERE b.eventId = :eventId AND b.eventWinnerId = :winnerId")
  List<BetEntity> findWinningBets(
      @Param("eventId") String eventId, @Param("winnerId") String winnerId);
}
