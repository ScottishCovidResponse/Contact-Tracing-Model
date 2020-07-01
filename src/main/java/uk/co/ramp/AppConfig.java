package uk.co.ramp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.io.readers.DirectoryList;
import uk.co.ramp.io.readers.DiseasePropertiesReader;
import uk.co.ramp.io.readers.FullPathInputFilesReader;
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
  private static final String DEFAULT_INPUT_FOLDER = "input";
  private static final String INPUT_FILE_LOCATION = "input/inputLocations.json";

  private final String seed;
  private final String overrideInputFolderLocation;

  AppConfig(
      @Value("${seed:#{null}}") String seed,
      @Value("${overrideInputFolderLocation:#{null}}") String overrideInputFolderLocation) {
    this.seed = seed;
    this.overrideInputFolderLocation = overrideInputFolderLocation;
  }

  @Bean
  public InputFiles inputFiles() {
    var baseInputFilesReader = new InputFilesReader();
    var directoryList = new DirectoryList();
    var overrideInputFolder =
        Optional.ofNullable(overrideInputFolderLocation).orElse(DEFAULT_INPUT_FOLDER);
    var inputFilesReader =
        new FullPathInputFilesReader(
            baseInputFilesReader, directoryList, overrideInputFolder, DEFAULT_INPUT_FOLDER);
    try (Reader reader = getReader(INPUT_FILE_LOCATION)) {
      return inputFilesReader.read(reader);
    } catch (IOException e) {
      String message =
          "An error occurred while parsing the run properties at " + INPUT_FILE_LOCATION;
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

  private void useSeed(RandomDataGenerator rdg, int seed) {
    rdg.reSeed(seed);
    LOGGER.info("Additional Seed information provided, the seed will be {}", seed);
  }

  private void logUsingDefaultSeed() {
    LOGGER.info("Additional Seed information not provided, using internal random seed.");
  }

  @Bean
  public RandomDataGenerator randomDataGenerator() {
    RandomDataGenerator rdg = new RandomDataGenerator();

    try {
      OptionalInt cmdLineSeedOverride =
          Stream.ofNullable(seed).mapToInt(Integer::parseInt).findAny();
      OptionalInt propertiesSeedOverride = standardProperties().seed();
      Stream.of(cmdLineSeedOverride, propertiesSeedOverride)
          .flatMapToInt(OptionalInt::stream)
          .reduce(Integer::sum)
          .ifPresentOrElse(seed -> useSeed(rdg, seed), this::logUsingDefaultSeed);

      return rdg;
    } catch (NullPointerException | ConfigurationException e) {
      String message =
          "An error occurred while creating the random generator. This is likely due to an error in Standard Properties";
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    } catch (NumberFormatException e) {
      String message =
          Optional.ofNullable(seed)
              .map(
                  seed ->
                      "An error occurred while creating the random generator. The command line arg, \""
                          + seed
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
