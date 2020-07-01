package uk.co.ramp;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.*;
import org.mockito.Mockito;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.InputFiles;
import uk.co.ramp.io.types.PopulationProperties;
import uk.co.ramp.io.types.StandardProperties;

public class AppConfigTest {

  @Rule public LogSpy appender = new LogSpy();
  AppConfig appConfig;

  @Before
  public void setUp() {
    appConfig = new AppConfig(null, null);
  }

  @Test
  public void standardProperties() throws ConfigurationException {
    StandardProperties standardProperties = appConfig.standardProperties();

    Assert.assertNotNull(standardProperties);
    //        Assert.assertFalse(standardProperties.contactsFile().isEmpty());
    Assert.assertNotNull(standardProperties.steadyState());
    Assert.assertTrue(standardProperties.initialExposures() > 0);
    Assert.assertTrue(standardProperties.populationSize() > 0);
    Assert.assertTrue(standardProperties.seed().isEmpty());
    Assert.assertTrue(standardProperties.timeLimit() > 0);
    Assert.assertTrue(standardProperties.populationSize() > standardProperties.initialExposures());
  }

  @Test(expected = ConfigurationException.class)
  public void standardPropertiesError() throws ConfigurationException, IOException {

    String file = File.createTempFile("temp", "file").getAbsolutePath();
    try {
      InputFiles input = mock(InputFiles.class);
      when(input.runSettings()).thenReturn(file);
      appConfig = mock(AppConfig.class);
      when(appConfig.getReader(anyString())).thenThrow(new FileNotFoundException());
      when(appConfig.standardProperties()).thenCallRealMethod();
      when(appConfig.inputFiles()).thenReturn(input);
      appConfig.standardProperties();
    } catch (ConfigurationException e) {
      Assert.assertThat(
          appender.getOutput(),
          containsString("An error occurred while parsing the run properties at " + file));
      throw e;
    }
  }

  @Test
  public void diseaseProperties() throws ConfigurationException {

    DiseaseProperties diseaseProperties = appConfig.diseaseProperties();

    Assert.assertNotNull(diseaseProperties);

    Assert.assertTrue(diseaseProperties.timeLatent().mean() > 0);
    Assert.assertTrue(diseaseProperties.timeRecoveryAsymp().mean() > 0);
    Assert.assertTrue(diseaseProperties.timeRecoverySymp().mean() > 0);
    Assert.assertTrue(diseaseProperties.timeRecoverySev().mean() > 0);
    Assert.assertTrue(diseaseProperties.timeSymptomsOnset().mean() > 0);
    Assert.assertTrue(diseaseProperties.timeDecline().mean() > 0);
    Assert.assertTrue(diseaseProperties.timeDeath().mean() > 0);
    Assert.assertTrue(diseaseProperties.timeTestAdministered().mean() > 0);
    Assert.assertTrue(diseaseProperties.timeTestResult().mean() > 0);

    Assert.assertTrue(diseaseProperties.timeLatent().max() > 0);
    Assert.assertTrue(diseaseProperties.timeRecoveryAsymp().max() > 0);
    Assert.assertTrue(diseaseProperties.timeRecoverySymp().max() > 0);
    Assert.assertTrue(diseaseProperties.timeRecoverySev().max() > 0);
    Assert.assertTrue(diseaseProperties.timeSymptomsOnset().max() > 0);
    Assert.assertTrue(diseaseProperties.timeDecline().max() > 0);
    Assert.assertTrue(diseaseProperties.timeDeath().max() > 0);
    Assert.assertTrue(diseaseProperties.timeTestAdministered().max() > 0);
    Assert.assertTrue(diseaseProperties.timeTestResult().max() > 0);

    Assert.assertTrue(diseaseProperties.testAccuracy() > 0);
    Assert.assertTrue(diseaseProperties.exposureThreshold() > 0);
    Assert.assertTrue(
        diseaseProperties.exposureProbability4UnitContact() > 0.
            && diseaseProperties.exposureProbability4UnitContact() < 1.);
    Assert.assertTrue(diseaseProperties.exposureExponent() > 0.);
    Assert.assertTrue(diseaseProperties.randomInfectionRate() >= 0);
    Assert.assertNotNull(diseaseProperties.progressionDistribution());
  }

  @Test(expected = ConfigurationException.class)
  public void diseasePropertiesError() throws IOException, ConfigurationException {
    String file = File.createTempFile("temp", "file").getAbsolutePath();
    try {
      InputFiles input = mock(InputFiles.class);
      when(input.diseaseSettings()).thenReturn(file);
      appConfig = mock(AppConfig.class);
      when(appConfig.getReader(anyString())).thenThrow(new FileNotFoundException());
      when(appConfig.diseaseProperties()).thenCallRealMethod();
      when(appConfig.inputFiles()).thenReturn(input);
      appConfig.diseaseProperties();
    } catch (ConfigurationException e) {
      Assert.assertThat(
          appender.getOutput(),
          containsString("An error occurred while parsing the disease properties at " + file));
      throw e;
    }
  }

  @Test
  public void populationProperties() throws ConfigurationException {

    PopulationProperties populationProperties = appConfig.populationProperties();
    Assert.assertTrue(populationProperties.genderBalance() > 0);
    Assert.assertNotNull(populationProperties.populationAges());
    Assert.assertNotNull(populationProperties.populationDistribution());
    Assert.assertEquals(
        populationProperties.populationAges().size(),
        populationProperties.populationDistribution().size());
  }

  @Test(expected = ConfigurationException.class)
  public void populationPropertiesError() throws IOException, ConfigurationException {
    String file = File.createTempFile("temp", "file").getAbsolutePath();
    try {
      InputFiles input = mock(InputFiles.class);
      when(input.populationSettings()).thenReturn(file);
      appConfig = mock(AppConfig.class);
      when(appConfig.getReader(anyString())).thenThrow(new FileNotFoundException());
      when(appConfig.populationProperties()).thenCallRealMethod();
      when(appConfig.inputFiles()).thenReturn(input);
      appConfig.populationProperties();
    } catch (ConfigurationException e) {
      Assert.assertThat(
          appender.getOutput(),
          containsString("An error occurred while parsing the population properties at " + file));
      throw e;
    }
  }

  @Test
  public void randomDataGeneratorNoArgs() throws ConfigurationException {

    RandomDataGenerator r = appConfig.randomDataGenerator();
    Assert.assertNotNull(r);
    Assert.assertThat(
        appender.getOutput(),
        containsString("Additional Seed information not provided, using internal random seed."));
  }

  @Test
  public void randomDataGeneratorWithArgs() throws ConfigurationException {

    Random random = TestUtils.getRandom();

    int arg = random.nextInt(10);
    appConfig = new AppConfig(String.valueOf(arg), "src/test/resources/testSeedOverride");

    RandomDataGenerator r = appConfig.randomDataGenerator();
    int seed = appConfig.standardProperties().seed().orElseThrow();
    Assert.assertNotNull(r);
    Assert.assertThat(
        appender.getOutput(),
        containsString("Additional Seed information provided, the seed will be " + (seed + arg)));
  }

  @Test(expected = ConfigurationException.class)
  public void randomDataGeneratorWithInvalidArgs() throws ConfigurationException {

    appConfig = new AppConfig("seed", null);
    try {
      appConfig.randomDataGenerator();
    } catch (ConfigurationException e) {
      Assert.assertThat(
          appender.getOutput(),
          containsString(
              "An error occurred while creating the random generator. The command line arg, \"seed\", could not be parsed as an integer."));
      throw e;
    }
  }

  @Test(expected = ConfigurationException.class)
  public void randomDataGeneratorWithInvalidSystemProperties() throws ConfigurationException {

    AppConfig appConfig = Mockito.mock(AppConfig.class);
    when(appConfig.randomDataGenerator()).thenCallRealMethod();
    try {
      appConfig.randomDataGenerator();

    } catch (ConfigurationException e) {
      Assert.assertThat(
          appender.getOutput(),
          containsString(
              "An error occurred while creating the random generator. This is likely due to an error in Standard Properties"));
      throw e;
    }
  }
}
