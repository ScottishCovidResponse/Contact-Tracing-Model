package uk.co.ramp;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Random;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.co.ramp.distribution.BoundedDistribution;
import uk.co.ramp.distribution.ImmutableBoundedDistribution;
import uk.co.ramp.io.readers.AgeDataReader;
import uk.co.ramp.io.types.*;
import uk.co.ramp.people.AgeRetriever;
import uk.ramp.distribution.Distribution;
import uk.ramp.distribution.Distribution.DistributionType;
import uk.ramp.distribution.ImmutableDistribution;
import uk.ramp.distribution.ImmutableMinMax;
import uk.ramp.distribution.MinMax;

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
  public static DiseaseProperties diseaseProperties() {
    Distribution dist =
        ImmutableDistribution.builder()
            .internalType(DistributionType.empirical)
            .internalScale(5)
            .empiricalSamples(List.of(5))
            .rng(dataGenerator().getRandomGenerator())
            .build();
    BoundedDistribution boundedDist =
        ImmutableBoundedDistribution.builder().distribution(dist).max(5).build();

    Distribution distTest =
        ImmutableDistribution.builder()
            .internalType(DistributionType.empirical)
            .empiricalSamples(List.of(2))
            .internalScale(2)
            .rng(dataGenerator().getRandomGenerator())
            .build();
    BoundedDistribution boundedDistTest =
        ImmutableBoundedDistribution.builder().distribution(distTest).max(3).build();

    return ImmutableDiseaseProperties.builder()
        .timeLatent(boundedDist)
        .timeRecoveryAsymp(boundedDist)
        .timeRecoverySymp(boundedDist)
        .timeRecoverySev(boundedDist)
        .timeSymptomsOnset(boundedDist)
        .timeDecline(boundedDist)
        .timeDeath(boundedDist)
        .timeTestAdministered(boundedDistTest)
        .timeTestResult(boundedDistTest)
        .testPositiveAccuracy(0.95)
        .testNegativeAccuracy(0.95)
        .exposureThreshold(500)
        .exposureProbability4UnitContact(0.01)
        .exposureExponent(1.)
        .randomInfectionRate(0.05)
        .build();
  }

  @Bean
  public static PopulationProperties populationProperties() {
    // index and proportion
    // data taken from census
    MinMax bin1 =
        ImmutableMinMax.builder()
            .lowerBoundary(0)
            .upperBoundary(19)
            .isLowerInclusive(true)
            .isUpperInclusive(true)
            .build();
    MinMax bin2 =
        ImmutableMinMax.builder()
            .lowerBoundary(20)
            .upperBoundary(39)
            .isLowerInclusive(true)
            .isUpperInclusive(true)
            .build();
    MinMax bin3 =
        ImmutableMinMax.builder()
            .lowerBoundary(40)
            .upperBoundary(59)
            .isLowerInclusive(true)
            .isUpperInclusive(true)
            .build();
    MinMax bin4 =
        ImmutableMinMax.builder()
            .lowerBoundary(60)
            .upperBoundary(79)
            .isLowerInclusive(true)
            .isUpperInclusive(true)
            .build();
    MinMax bin5 =
        ImmutableMinMax.builder()
            .lowerBoundary(80)
            .upperBoundary(100)
            .isLowerInclusive(true)
            .isUpperInclusive(true)
            .build();
    Distribution distribution =
        ImmutableDistribution.builder()
            .internalType(DistributionType.categorical)
            .addBins(bin1, bin2, bin3, bin4, bin5)
            .addWeights(0.2, 0.2, 0.2, 0.2, 0.2)
            .rng(dataGenerator().getRandomGenerator())
            .build();

    return ImmutablePopulationProperties.builder()
        .distribution(distribution)
        .genderBalance(0.99)
        .testCapacity(0.01)
        .appUptake(0.7)
        .build();
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
    return new AgeRetriever(populationProperties(), agesData);
  }

  public static AgeDependentHealthList ageDependentHealth() {
    return ImmutableAgeDependentHealthList.builder()
        .addAgeDependentList(
            ImmutableAgeDependentHealth.builder()
                .range(uk.co.ramp.utilities.ImmutableMinMax.of(0, 19))
                .modifier(1.d)
                .build())
        .addAgeDependentList(
            ImmutableAgeDependentHealth.builder()
                .range(uk.co.ramp.utilities.ImmutableMinMax.of(20, 39))
                .modifier(0.9)
                .build())
        .addAgeDependentList(
            ImmutableAgeDependentHealth.builder()
                .range(uk.co.ramp.utilities.ImmutableMinMax.of(40, 59))
                .modifier(0.8)
                .build())
        .addAgeDependentList(
            ImmutableAgeDependentHealth.builder()
                .range(uk.co.ramp.utilities.ImmutableMinMax.of(60, 79))
                .modifier(0.6)
                .build())
        .addAgeDependentList(
            ImmutableAgeDependentHealth.builder()
                .range(uk.co.ramp.utilities.ImmutableMinMax.of(80, 100))
                .modifier(0.4)
                .build())
        .build();
  }
}
