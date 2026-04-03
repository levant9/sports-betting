package com.sportsbetting.settlement.config;

import com.sportsbetting.settlement.domain.BetEntity;
import com.sportsbetting.settlement.repository.BetRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

  private final BetRepository betRepository;

  @Bean
  @Transactional
  public CommandLineRunner initializeSampleData() {
    return args -> {
      log.info("Initializing sample bet data...");

      BetEntity bet1 =
          new BetEntity("BET001", "USER001", "E1", "MARKET1", "T1", new BigDecimal("100.00"));
      BetEntity bet2 =
          new BetEntity("BET002", "USER002", "E1", "MARKET1", "T2", new BigDecimal("50.00"));
      BetEntity bet3 =
          new BetEntity("BET003", "USER003", "E1", "MARKET1", "T1", new BigDecimal("75.00"));
      BetEntity bet4 =
          new BetEntity("BET004", "USER004", "E2", "MARKET2", "T3", new BigDecimal("200.00"));
      BetEntity bet5 =
          new BetEntity("BET005", "USER005", "E2", "MARKET2", "T4", new BigDecimal("150.00"));

      betRepository.save(bet1);
      betRepository.save(bet2);
      betRepository.save(bet3);
      betRepository.save(bet4);
      betRepository.save(bet5);

      log.info("Sample bet data initialized successfully");
    };
  }
}
