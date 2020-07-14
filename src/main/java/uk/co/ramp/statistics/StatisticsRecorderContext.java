package uk.co.ramp.statistics;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
public class StatisticsRecorderContext {

  @Bean
  StatisticsRecorder statisticsRecorder() {
    return new StatisticsRecorderImpl();
  }
}
