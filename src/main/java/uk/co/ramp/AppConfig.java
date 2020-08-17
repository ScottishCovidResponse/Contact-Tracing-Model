package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.io.InfectionRates;
import uk.co.ramp.io.readers.*;
import uk.co.ramp.io.types.*;
import uk.co.ramp.people.AgeRetriever;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

@SpringBootConfiguration
@ComponentScan
public class AppConfig {

  private static final Logger LOGGER = LogManager.getLogger(AppConfig.class);
  private static final String DEFAULT_INPUT_FOLDER = "input";
  private static final String DEFAULT_OUTPUT_FOLDER = "output";
  private static final String INPUT_FILE_LOCATION = "input/inputLocations.json";

  private final String seed;
  private final String overrideInputFolderLocation;
  private final String overrideOutputFolderLocation;

  AppConfig(
      @Value("${seed:#{null}}") String seed,
      @Value("${overrideInputFolderLocation:#{null}}") String overrideInputFolderLocation,
      @Value("${overrideOutputFolderLocation:#{null}}") String overrideOutputFolderLocation) {
    this.seed = seed;
    this.overrideInputFolderLocation = overrideInputFolderLocation;
    this.overrideOutputFolderLocation = overrideOutputFolderLocation;
  }

  @Bean
  public OutputFolder outputFolder() {
    String overrideOutputFolder =
        Optional.ofNullable(overrideOutputFolderLocation).orElse(DEFAULT_OUTPUT_FOLDER);
    try {
      Files.createDirectories(Paths.get(overrideOutputFolder));
      return ImmutableOutputFolder.builder().outputFolder(new File(overrideOutputFolder)).build();
    } catch (IOException e) {
      String message = "An error occurred creating output folder at " + overrideOutputFolder;
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    }
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

  @Bean
  public AgeRetriever ageRetriever(
      PopulationProperties populationProperties, RandomDataGenerator randomDataGenerator) {
    try (Reader reader = getReader(inputFiles().ageData())) {
      var ageData = new AgeDataReader().read(reader);
      return new AgeRetriever(populationProperties, randomDataGenerator, ageData);
    } catch (IOException e) {
      String message = "An error occurred while parsing the age data at " + inputFiles().ageData();
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    }
  }

  @Bean
  public InfectionRates infectionRates(){
    try (Reader reader = getReader(inputFiles().infectionRates())) {
      return new InfectionRateReader().read(reader);
    } catch (IOException e) {
      String message = "An error occurred while parsing the infection rates at " + inputFiles().infectionRates();
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
      if (seed == null && standardProperties().seed().isEmpty()) {
        logUsingDefaultSeed();
      } else {
        int cmdLineSeed = seed == null ? 0 : Integer.parseInt(seed);
        int propertiesSeed = standardProperties().seed().orElse(0);
        useSeed(rdg, cmdLineSeed + propertiesSeed);
      }
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
