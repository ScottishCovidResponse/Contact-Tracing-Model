package uk.co.ramp.io.readers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.io.types.PopulationProperties;
import uk.ramp.api.StandardApi;
import uk.ramp.distribution.Distribution;
import uk.ramp.distribution.Distribution.DistributionType;
import uk.ramp.distribution.ImmutableDistribution;
import uk.ramp.distribution.ImmutableMinMax;
import uk.ramp.distribution.MinMax;

public class PopulationPropertiesReaderTest {
  private final MinMax bin1 =
      ImmutableMinMax.builder()
          .lowerBoundary(0)
          .upperBoundary(14)
          .isLowerInclusive(true)
          .isUpperInclusive(true)
          .build();
  private final MinMax bin2 =
      ImmutableMinMax.builder()
          .lowerBoundary(15)
          .upperBoundary(24)
          .isLowerInclusive(true)
          .isUpperInclusive(true)
          .build();
  private final MinMax bin3 =
      ImmutableMinMax.builder()
          .lowerBoundary(25)
          .upperBoundary(54)
          .isLowerInclusive(true)
          .isUpperInclusive(true)
          .build();
  private final MinMax bin4 =
      ImmutableMinMax.builder()
          .lowerBoundary(55)
          .upperBoundary(64)
          .isLowerInclusive(true)
          .isUpperInclusive(true)
          .build();
  private final MinMax bin5 =
      ImmutableMinMax.builder()
          .lowerBoundary(65)
          .upperBoundary(90)
          .isLowerInclusive(true)
          .isUpperInclusive(true)
          .build();
  private final Distribution distribution =
      ImmutableDistribution.builder()
          .internalType(DistributionType.categorical)
          .addBins(bin1, bin2, bin3, bin4, bin5)
          .addWeights(0.1759, 0.1171, 0.4029, 0.1222, 0.1819)
          .rng(mock(RandomGenerator.class))
          .build();

  private StandardApi dataPipelineApi;

  @Before
  public void setUp() {
    this.dataPipelineApi = mock(StandardApi.class);
    when(dataPipelineApi.readDistribution(
            eq("population_parameters"), eq("population-distribution")))
        .thenReturn(distribution);
    when(dataPipelineApi.readEstimate(eq("population_parameters"), eq("gender-balance")))
        .thenReturn(0.99);
    when(dataPipelineApi.readEstimate(eq("population_parameters"), eq("app-uptake")))
        .thenReturn(0.7);
    when(dataPipelineApi.readEstimate(eq("population_parameters"), eq("test-capacity")))
        .thenReturn(0.01);
  }

  @Test
  public void testRead() {
    var reader = new PopulationPropertiesReader(dataPipelineApi);
    PopulationProperties populationProperties = reader.read();

    assertThat(populationProperties.genderBalance()).isCloseTo(0.99, offset(1e-6));
    assertThat(populationProperties.appUptake()).isCloseTo(0.7, offset(1e-6));
    assertThat(populationProperties.testCapacity()).isCloseTo(0.01, offset(1e-6));

    assertThat(populationProperties.distribution()).isEqualTo(distribution);
  }
}
