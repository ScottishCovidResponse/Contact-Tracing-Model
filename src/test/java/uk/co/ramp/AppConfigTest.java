package uk.co.ramp;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.InputFiles;
import uk.co.ramp.io.types.PopulationProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.ramp.api.StandardApi;
import uk.ramp.distribution.Distribution;
import uk.ramp.distribution.Distribution.DistributionType;
import uk.ramp.distribution.ImmutableDistribution;
import uk.ramp.distribution.ImmutableMinMax;
import uk.ramp.distribution.MinMax;

public class AppConfigTest {

  @Rule public final LogSpy appender = new LogSpy();
  AppConfig appConfig;
  private StandardApi stdApi;

  @Before
  public void setUp() {
    RandomGenerator rng =
        new RandomDataGenerator().getRandomGenerator(); // TODO ideally we would want to mock this
    MinMax bin1 =
        ImmutableMinMax.builder()
            .lowerBoundary(0)
            .upperBoundary(14)
            .isLowerInclusive(true)
            .isUpperInclusive(true)
            .build();
    MinMax bin2 =
        ImmutableMinMax.builder()
            .lowerBoundary(15)
            .upperBoundary(24)
            .isLowerInclusive(true)
            .isUpperInclusive(true)
            .build();
    MinMax bin3 =
        ImmutableMinMax.builder()
            .lowerBoundary(25)
            .upperBoundary(54)
            .isLowerInclusive(true)
            .isUpperInclusive(true)
            .build();
    MinMax bin4 =
        ImmutableMinMax.builder()
            .lowerBoundary(55)
            .upperBoundary(64)
            .isLowerInclusive(true)
            .isUpperInclusive(true)
            .build();
    MinMax bin5 =
        ImmutableMinMax.builder()
            .lowerBoundary(65)
            .upperBoundary(90)
            .isLowerInclusive(true)
            .isUpperInclusive(true)
            .build();
    Distribution populationDistribution =
        ImmutableDistribution.builder()
            .internalType(DistributionType.categorical)
            .addBins(bin1, bin2, bin3, bin4, bin5)
            .addWeights(0.1759, 0.1171, 0.4029, 0.1222, 0.1819)
            .rng(rng)
            .build();

    stdApi = mock(StandardApi.class);
    when(stdApi.readEstimate(anyString(), eq("test-positive-accuracy"))).thenReturn(0.9);
    when(stdApi.readEstimate(anyString(), eq("test-negative-accuracy"))).thenReturn(0.9);
    when(stdApi.readEstimate(anyString(), eq("exposure-threshold"))).thenReturn(50.0);
    when(stdApi.readEstimate(anyString(), eq("exposure-probability-4-unit-contact")))
        .thenReturn(0.01);
    when(stdApi.readEstimate(anyString(), eq("exposure-exponent"))).thenReturn(1.0);
    when(stdApi.readEstimate(anyString(), eq("random-infection-rate"))).thenReturn(0.0005);
    when(stdApi.readEstimate(anyString(), eq("time-latent-max"))).thenReturn(8);
    when(stdApi.readEstimate(anyString(), eq("time-recovery-asymp-max"))).thenReturn(8);
    when(stdApi.readEstimate(anyString(), eq("time-recovery-symp-max"))).thenReturn(8);
    when(stdApi.readEstimate(anyString(), eq("time-recovery-sev-max"))).thenReturn(8);
    when(stdApi.readEstimate(anyString(), eq("time-symptoms-onset-max"))).thenReturn(8);
    when(stdApi.readEstimate(anyString(), eq("time-decline-max"))).thenReturn(8);
    when(stdApi.readEstimate(anyString(), eq("time-death-max"))).thenReturn(8);
    when(stdApi.readEstimate(anyString(), eq("time-test-administered-max"))).thenReturn(3);
    when(stdApi.readEstimate(anyString(), eq("time-test-result-max"))).thenReturn(3);
    when(stdApi.readDistribution(anyString(), eq("time-latent-distribution")))
        .thenReturn(
            ImmutableDistribution.builder()
                .internalType(DistributionType.exponential)
                .internalScale(5.0)
                .rng(rng)
                .build());
    when(stdApi.readDistribution(anyString(), eq("time-recovery-asymp-distribution")))
        .thenReturn(
            ImmutableDistribution.builder()
                .internalType(DistributionType.exponential)
                .internalScale(5.0)
                .rng(rng)
                .build());
    when(stdApi.readDistribution(anyString(), eq("time-recovery-symp-distribution")))
        .thenReturn(
            ImmutableDistribution.builder()
                .internalType(DistributionType.exponential)
                .internalScale(5.0)
                .rng(rng)
                .build());
    when(stdApi.readDistribution(anyString(), eq("time-recovery-sev-distribution")))
        .thenReturn(
            ImmutableDistribution.builder()
                .internalType(DistributionType.exponential)
                .internalScale(5.0)
                .rng(rng)
                .build());
    when(stdApi.readDistribution(anyString(), eq("time-symptoms-onset-distribution")))
        .thenReturn(
            ImmutableDistribution.builder()
                .internalType(DistributionType.exponential)
                .internalScale(5.0)
                .rng(rng)
                .build());
    when(stdApi.readDistribution(anyString(), eq("time-decline-distribution")))
        .thenReturn(
            ImmutableDistribution.builder()
                .internalType(DistributionType.exponential)
                .internalScale(5.0)
                .rng(rng)
                .build());
    when(stdApi.readDistribution(anyString(), eq("time-death-distribution")))
        .thenReturn(
            ImmutableDistribution.builder()
                .internalType(DistributionType.exponential)
                .internalScale(5.0)
                .rng(rng)
                .build());
    when(stdApi.readDistribution(anyString(), eq("time-test-administered-distribution")))
        .thenReturn(
            ImmutableDistribution.builder()
                .internalType(DistributionType.exponential)
                .internalScale(1.0)
                .rng(rng)
                .build());
    when(stdApi.readDistribution(anyString(), eq("time-test-result-distribution")))
        .thenReturn(
            ImmutableDistribution.builder()
                .internalType(DistributionType.exponential)
                .internalScale(1.0)
                .rng(rng)
                .build());
    when(stdApi.readEstimate(anyString(), eq("gender-balance"))).thenReturn(0.99);
    when(stdApi.readDistribution(anyString(), eq("population-distribution")))
        .thenReturn(populationDistribution);
    when(stdApi.readEstimate(anyString(), eq("app-uptake"))).thenReturn(0.01);
    when(stdApi.readEstimate(anyString(), eq("test-capacity"))).thenReturn(0.7);

    appConfig = new AppConfig(null, null, null);
  }

  @Test
  public void standardProperties() throws ConfigurationException {
    StandardProperties standardProperties = appConfig.standardProperties();

    Assert.assertNotNull(standardProperties);
    Assert.assertTrue(standardProperties.initialExposures() > 0);
    Assert.assertTrue(standardProperties.populationSize() > 0);
    Assert.assertTrue(standardProperties.seed().isEmpty());
    Assert.assertTrue(standardProperties.timeLimitDays() > 0);
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

    DiseaseProperties diseaseProperties = appConfig.diseaseProperties(stdApi);

    Assert.assertNotNull(diseaseProperties);

    Assert.assertTrue(diseaseProperties.timeLatent().getDistributionValue() > 0);
    Assert.assertTrue(diseaseProperties.timeRecoveryAsymp().getDistributionValue() > 0);
    Assert.assertTrue(diseaseProperties.timeRecoverySymp().getDistributionValue() > 0);
    Assert.assertTrue(diseaseProperties.timeRecoverySev().getDistributionValue() > 0);
    Assert.assertTrue(diseaseProperties.timeSymptomsOnset().getDistributionValue() > 0);
    Assert.assertTrue(diseaseProperties.timeDecline().getDistributionValue() > 0);
    Assert.assertTrue(diseaseProperties.timeDeath().getDistributionValue() > 0);
    Assert.assertTrue(diseaseProperties.timeTestAdministered().getDistributionValue() > 0);
    Assert.assertTrue(diseaseProperties.timeTestResult().getDistributionValue() > 0);

    Assert.assertTrue(diseaseProperties.timeLatent().max() > 0);
    Assert.assertTrue(diseaseProperties.timeRecoveryAsymp().max() > 0);
    Assert.assertTrue(diseaseProperties.timeRecoverySymp().max() > 0);
    Assert.assertTrue(diseaseProperties.timeRecoverySev().max() > 0);
    Assert.assertTrue(diseaseProperties.timeSymptomsOnset().max() > 0);
    Assert.assertTrue(diseaseProperties.timeDecline().max() > 0);
    Assert.assertTrue(diseaseProperties.timeDeath().max() > 0);
    Assert.assertTrue(diseaseProperties.timeTestAdministered().max() > 0);
    Assert.assertTrue(diseaseProperties.timeTestResult().max() > 0);

    Assert.assertTrue(diseaseProperties.testPositiveAccuracy() > 0);
    Assert.assertTrue(diseaseProperties.testNegativeAccuracy() > 0);
    Assert.assertTrue(diseaseProperties.exposureThreshold() > 0);
    Assert.assertTrue(
        diseaseProperties.exposureProbability4UnitContact() > 0.
            && diseaseProperties.exposureProbability4UnitContact() < 1.);
    Assert.assertTrue(diseaseProperties.exposureExponent() > 0.);
    Assert.assertTrue(diseaseProperties.randomInfectionRate() >= 0);
  }

  @Test(expected = ConfigurationException.class)
  public void diseasePropertiesError() throws ConfigurationException {
    StandardApi dataApi = mock(StandardApi.class);
    when(dataApi.readEstimate(anyString(), anyString())).thenThrow(ConfigurationException.class);
    when(dataApi.readDistribution(anyString(), anyString()))
        .thenThrow(ConfigurationException.class);

    appConfig = new AppConfig(null, null, null);
    appConfig.diseaseProperties(dataApi);
  }

  @Test
  public void populationProperties() throws ConfigurationException {

    PopulationProperties populationProperties = appConfig.populationProperties(stdApi);
    Assert.assertTrue(populationProperties.genderBalance() > 0);
    Assert.assertNotNull(populationProperties.distribution());
    Assert.assertNotEquals(populationProperties.genderBalance(), 0);
  }

  @Test(expected = ConfigurationException.class)
  public void populationPropertiesError() throws IOException, ConfigurationException {
    StandardApi stdApi = mock(StandardApi.class);
    when(stdApi.readDistribution(anyString(), anyString())).thenThrow(ConfigurationException.class);
    when(stdApi.readEstimate(anyString(), anyString())).thenThrow(ConfigurationException.class);
    appConfig = new AppConfig(null, null, null);
    appConfig.populationProperties(stdApi);
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
    appConfig =
        new AppConfig(
            String.valueOf(arg),
            "src/test/resources/testSeedOverride",
            "src/test/resources/testSeedOverride");

    RandomDataGenerator r = appConfig.randomDataGenerator();
    int seed = appConfig.standardProperties().seed().orElseThrow();
    Assert.assertNotNull(r);
    Assert.assertThat(
        appender.getOutput(),
        containsString("Additional Seed information provided, the seed will be " + (seed + arg)));
  }

  @Test(expected = ConfigurationException.class)
  public void randomDataGeneratorWithInvalidArgs() throws ConfigurationException {

    appConfig = new AppConfig("seed", null, null);
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
