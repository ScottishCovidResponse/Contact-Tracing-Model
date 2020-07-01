package uk.co.ramp.policy.alert;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import uk.co.ramp.ConfigurationException;
import uk.co.ramp.Population;
import uk.co.ramp.event.CompletionEventListGroup;
import uk.co.ramp.io.types.InputFiles;

@SpringBootConfiguration
public class TracingPolicyContext {
  private static final Logger LOGGER = LogManager.getLogger(TracingPolicyContext.class);

  @Bean
  AlertContactTracer contactTracer(
      CompletionEventListGroup eventList, Population population, TracingPolicy tracingPolicy) {
    return new AlertContactTracer(tracingPolicy, eventList, population);
  }

  @Bean
  public AlertChecker alertChecker(
      AlertContactTracer alertContactTracer, Population population, TracingPolicy tracingPolicy) {
    return new AlertChecker(tracingPolicy, alertContactTracer, population);
  }

  @Bean
  TracingPolicy tracingPolicy(InputFiles inputFiles) {
    String location = inputFiles.tracingPolicies();
    try (Reader reader = new FileReader(new File(location))) {
      return new TracingPolicyReader().read(reader);
    } catch (IOException e) {
      String message =
          "An error occurred while parsing the tracing policy properties at " + location;
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    }
  }
}