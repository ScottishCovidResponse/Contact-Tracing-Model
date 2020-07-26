package uk.co.ramp.statistics;

import java.util.EnumMap;
import java.util.HashMap;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.AlertStatus;

@SpringBootConfiguration
public class StatisticsRecorderContext {

  @Bean
  StatisticsRecorder statisticsRecorder(StandardProperties properties) {
    return new StatisticsRecorderImpl(
        properties,
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new EnumMap<>(AlertStatus.class),
        new EnumMap<>(AlertStatus.class));
  }

  @Bean
  StatisticsWriter statisticsWriter(
      StatisticsRecorder statisticsRecorder, StandardProperties properties) {
    return new StatisticsWriter(statisticsRecorder, properties);
  }
}
