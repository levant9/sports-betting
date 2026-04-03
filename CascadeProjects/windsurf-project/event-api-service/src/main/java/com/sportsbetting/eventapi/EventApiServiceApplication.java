package com.sportsbetting.eventapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventApiServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(EventApiServiceApplication.class, args);
  }
}
