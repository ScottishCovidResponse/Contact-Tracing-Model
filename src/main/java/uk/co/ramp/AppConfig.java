package uk.co.ramp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.io.readers.DiseasePropertiesReader;
import uk.co.ramp.io.readers.InputFilesReader;
import uk.co.ramp.io.readers.PopulationPropertiesReader;
import uk.co.ramp.io.readers.StandardPropertiesReader;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.InputFiles;
import uk.co.ramp.io.types.PopulationProperties;
import uk.co.ramp.io.types.StandardProperties;

@SpringBootConfiguration
@ComponentScan
public class AppConfig {

  private static final Logger LOGGER = LogManager.getLogger(AppConfig.class);
  private static final String INPUT_FILE = "input/inputLocations.json";

  @Bean
  public InputFiles inputFiles() {
    try (Reader reader = getReader(INPUT_FILE)) {
      return new InputFilesReader().read(reader);
    } catch (IOException e) {
      String message = "An error occurred while parsing the run properties at " + INPUT_FILE;
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    }
  }

  @Bean
  public StandardProperties standardProperties() {
    try (Reader reader = getReader(inputFiles().runSettings())) {
      return new StandardPropertiesReader().read(reader);
    } catch (IOException e) {
      String message =
          "An error occurred while parsing the run properties at " + inputFiles().runSettings();
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    }
  }

  @Bean
  public DiseaseProperties diseaseProperties() {
    try (Reader reader = getReader(inputFiles().diseaseSettings())) {
      return new DiseasePropertiesReader().read(reader);
    } catch (IOException e) {
      String message =
          "An error occurred while parsing the disease properties at "
              + inputFiles().diseaseSettings();
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    }
  }

  @Bean
  public PopulationProperties populationProperties() {
    try (Reader reader = getReader(inputFiles().populationSettings())) {
      return new PopulationPropertiesReader().read(reader);
    } catch (IOException e) {
      String message =
          "An error occurred while parsing the population properties at "
              + inputFiles().populationSettings();
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    }
  }

  @Bean
  public RandomDataGenerator randomDataGenerator(
      @Value("${cmdLineArgument:#{null}}") Optional<String[]> argumentValue) {
    long arg = 0;
    try {
      if (argumentValue.isPresent() && argumentValue.get().length > 0) {
        arg = Integer.parseInt(argumentValue.get()[0]);
        LOGGER.info(
            "Additional Seed information provided, the seed will be {}",
            standardProperties().seed() + arg);
      } else {
        LOGGER.info(
            "Additional Seed information not provided, defaulting to {}",
            standardProperties().seed());
      }
      RandomDataGenerator r = new RandomDataGenerator();
      r.reSeed(standardProperties().seed() + arg);
      return r;
    } catch (NullPointerException | ConfigurationException e) {
      String message =
          "An error occurred while creating the random generator. This is likely due to an error in Standard Properties";
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    } catch (NumberFormatException e) {
      String message =
          argumentValue
              .map(
                  strings ->
                      "An error occurred while creating the random generator. The command line arg, \""
                          + strings[0]
                          + "\", could not be parsed as an integer.")
              .orElse(
                  "An unknown NumberFormatException occurred while creating the random number generator.");
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    }
  }

  @Bean
  public DistributionSampler distributionSampler(RandomDataGenerator randomDataGenerator) {
    return new DistributionSampler(randomDataGenerator);
  }

  Reader getReader(String input) throws FileNotFoundException {
    return new FileReader(new File(input));
  }
}
