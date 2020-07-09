package uk.co.ramp.policy.isolation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import uk.co.ramp.ConfigurationException;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.InputFiles;
import uk.co.ramp.io.types.StandardProperties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

@SpringBootConfiguration
public class IsolationPolicyContext {
  private static final Logger LOGGER = LogManager.getLogger(IsolationPolicyContext.class);

  @Bean
  public IsolationPolicy isolationPolicy(
          StandardProperties standardProperties,
      DiseaseProperties diseaseProperties,
      DistributionSampler distributionSampler,
      IsolationProperties isolationProperties) {
    var singleCaseIsolationPolicy =
        new SingleCaseIsolationPolicy(isolationProperties, distributionSampler, standardProperties);
    return new ContactIsolationPolicy(singleCaseIsolationPolicy, diseaseProperties);
  }

  @Bean
  IsolationProperties isolationProperties(InputFiles inputFiles) {
    String location = inputFiles.isolationPolicies();
    try (Reader reader = new FileReader(new File(location))) {
      return new IsolationPropertiesReader().read(reader);
    } catch (IOException e) {
      String message =
          "An error occurred while parsing the isolation policy properties at " + location;
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    }
  }
}
