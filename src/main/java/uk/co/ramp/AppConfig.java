package uk.co.ramp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.OptionalLong;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.io.readers.AgeDataReader;
import uk.co.ramp.io.readers.DirectoryList;
import uk.co.ramp.io.readers.DiseasePropertiesReader;
import uk.co.ramp.io.readers.FullPathInputFilesReader;
import uk.co.ramp.io.readers.InputFilesReader;
import uk.co.ramp.io.readers.PopulationPropertiesReader;
import uk.co.ramp.io.readers.StandardPropertiesReader;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.ImmutableOutputFolder;
import uk.co.ramp.io.types.InputFiles;
import uk.co.ramp.io.types.OutputFolder;
import uk.co.ramp.io.types.PopulationProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.AgeRetriever;
import uk.ramp.api.StandardApi;

@SpringBootConfiguration
@ComponentScan
public class AppConfig {

  private static final Logger LOGGER = LogManager.getLogger(AppConfig.class);
  private static final String DEFAULT_INPUT_FOLDER = "input";
  private static final String DEFAULT_OUTPUT_FOLDER = "output";
  private static final String INPUT_FILE_LOCATION = "input/inputLocations.json";
  private static final String CONFIG_FILE_LOCATION = "input/config.yaml";

  private final String seed;
  private final String overrideInputFolderLocation;
  private final String overrideOutputFolderLocation;

  AppConfig(
      @Value("${seed:#{null}}") String seed,
      @Value("${overrideInputFolderLocation:#{null}}") String overrideInputFolderLocation,
      @Value("${overrideOutputFolderLocation:#{null}}") String overrideOutputFolderLocation) {
    this.overrideInputFolderLocation = overrideInputFolderLocation;
    this.overrideOutputFolderLocation = overrideOutputFolderLocation;
    this.seed = seed;
  }

  @Bean
  public StandardApi dataPipelineApi(RandomDataGenerator randomDataGenerator) {
    return new StandardApi(Path.of(CONFIG_FILE_LOCATION), randomDataGenerator.getRandomGenerator());
  }

  private OptionalLong parsedSeed() {
    try {
      if (seed == null && standardProperties().seed().isEmpty()) {
        LOGGER.info("Additional Seed information not provided, using internal random seed.");
        return OptionalLong.empty();
      } else {
        int cmdLineSeed = seed == null ? 0 : Integer.parseInt(seed);
        int propertiesSeed = standardProperties().seed().orElse(0);
        long parsedSeed = cmdLineSeed + propertiesSeed;
        LOGGER.info("Additional Seed information provided, the seed will be {}", parsedSeed);
        return OptionalLong.of(parsedSeed);
      }
    } catch (NullPointerException | ConfigurationException e) {
      String message =
          "An error occurred while creating the random generator. This is likely due to an error in Standard Properties";
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    } catch (NumberFormatException e) {
      String message =
          Optional.ofNullable(seed)
              .map(
                  s ->
                      "An error occurred while creating the random generator. The command line arg, \""
                          + s
                          + "\", could not be parsed as an integer.")
              .orElse(
                  "An unknown NumberFormatException occurred while creating the random number generator.");
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    }
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
  public DiseaseProperties diseaseProperties(StandardApi dataPipelineApi) {
    var seed = parsedSeed();
    if (seed.isPresent()) {
      return new DiseasePropertiesReader(dataPipelineApi, seed.getAsLong()).read();
    } else {
      return new DiseasePropertiesReader(dataPipelineApi).read();
    }
  }

  @Bean
  public PopulationProperties populationProperties(StandardApi dataPipelineApi) {
    var seed = parsedSeed();
    if (seed.isPresent()) {
      return new PopulationPropertiesReader(dataPipelineApi, seed.getAsLong()).read();
    } else {
      return new PopulationPropertiesReader(dataPipelineApi).read();
    }
  }

  @Bean
  public AgeRetriever ageRetriever(PopulationProperties populationProperties) {
    try (Reader reader = getReader(inputFiles().ageData())) {
      var ageData = new AgeDataReader().read(reader);
      return new AgeRetriever(populationProperties, ageData);
    } catch (IOException e) {
      String message = "An error occurred while parsing the age data at " + inputFiles().ageData();
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    }
  }

  @Bean
  public RandomDataGenerator randomDataGenerator() {
    var seed = parsedSeed();
    RandomDataGenerator rdg = new RandomDataGenerator();

    if (seed.isPresent()) {
      rdg.reSeed(seed.getAsLong());
    }
    return rdg;
  }

  @Bean
  public DistributionSampler distributionSampler(RandomDataGenerator randomDataGenerator) {
    return new DistributionSampler(randomDataGenerator);
  }

  Reader getReader(String input) throws FileNotFoundException {
    return new FileReader(new File(input));
  }
}
