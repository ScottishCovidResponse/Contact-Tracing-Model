package uk.co.ramp;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Random;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.co.ramp.io.readers.AgeDataReader;
import uk.co.ramp.io.readers.DiseasePropertiesReader;
import uk.co.ramp.io.readers.PopulationPropertiesReader;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.ImmutableStandardProperties;
import uk.co.ramp.io.types.PopulationProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.AgeRetriever;

@TestConfiguration
public class TestUtils {

  public static Random getRandom() {
    return new Random(123);
  }

  public static RandomDataGenerator dataGenerator() {
    RandomDataGenerator r = new RandomDataGenerator();
    r.reSeed(123);
    return r;
  }

  @Bean
  public static DiseaseProperties diseaseProperties() throws FileNotFoundException {

    String file = TestUtils.class.getResource("/diseaseSettings.json").getFile();
    Reader reader = new FileReader(file);
    return new DiseasePropertiesReader().read(reader);
  }

  @Bean
  public static PopulationProperties populationProperties() throws FileNotFoundException {
    String file = TestUtils.class.getResource("/populationSettings.json").getFile();
    Reader reader = new FileReader(file);
    return new PopulationPropertiesReader().read(reader);
  }

  @Bean
  public static StandardProperties standardProperties() {
    return ImmutableStandardProperties.builder()
        .initialExposures(10)
        .populationSize(1000)
        .seed(123)
        .steadyState(true)
        .timeLimitDays(100)
        .timeStepsPerDay(1)
        .timeStepSpread(1)
        .build();
  }

  @Bean
  public static AgeRetriever ageRetriever() throws IOException {
    String file = TestUtils.class.getResource("/ageData.csv").getFile();
    Reader reader = new FileReader(file);
    var agesData = new AgeDataReader().read(reader);
    return new AgeRetriever(populationProperties(), dataGenerator(), agesData);
  }
}
