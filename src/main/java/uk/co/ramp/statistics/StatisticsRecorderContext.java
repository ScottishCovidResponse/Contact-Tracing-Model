package uk.co.ramp.statistics;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import uk.co.ramp.io.types.StandardProperties;

@SpringBootConfiguration
public class StatisticsRecorderContext {

  @Bean
  StatisticsRecorder statisticsRecorder() {
    return new StatisticsRecorderImpl();
  }

  @Bean
  StatisticsWriter statisticsWriter(
      StatisticsRecorder statisticsRecorder, StandardProperties properties) {
    return new StatisticsWriter(statisticsRecorder, properties);
  }
}
