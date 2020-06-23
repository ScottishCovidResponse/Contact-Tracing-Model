package uk.co.ramp.policy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import uk.co.ramp.ConfigurationException;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.io.types.DiseaseProperties;

@SpringBootConfiguration
public class IsolationPolicyContext {
  private static final String ISOLATION_SETTINGS_LOCATION = "input/isolationPolicies.json";
  private static final Logger LOGGER = LogManager.getLogger(IsolationPolicyContext.class);

  @Bean
  public IsolationPolicy isolationPolicy(
      DiseaseProperties diseaseProperties, DistributionSampler distributionSampler) {
    var singleCaseIsolationPolicy =
        new SingleCaseIsolationPolicy(isolationProperties(), distributionSampler);
    return new ContactIsolationPolicy(singleCaseIsolationPolicy, diseaseProperties);
  }

  private IsolationProperties isolationProperties() {
    try (Reader reader = new FileReader(new File(ISOLATION_SETTINGS_LOCATION))) {
      return new IsolationPropertiesReader().read(reader);
    } catch (IOException e) {
      String message =
          "An error occurred while parsing the isolation policy properties at "
              + ISOLATION_SETTINGS_LOCATION;
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    }
  }
}
