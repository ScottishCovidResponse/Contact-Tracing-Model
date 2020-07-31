package uk.co.ramp.io.readers;

import uk.co.ramp.io.types.ImmutablePopulationProperties;
import uk.co.ramp.io.types.PopulationProperties;
import uk.ramp.api.StandardApi;
import uk.ramp.distribution.Distribution;

public class PopulationPropertiesReader {
  private final StandardApi dataPipelineApi;
  private final boolean isSeeded;
  private final long seed;

  public PopulationPropertiesReader(StandardApi dataPipelineApi, long seed) {
    this.dataPipelineApi = dataPipelineApi;
    this.isSeeded = true;
    this.seed = seed;
  }

  public PopulationPropertiesReader(StandardApi dataPipelineApi) {
    this.dataPipelineApi = dataPipelineApi;
    this.isSeeded = false;
    this.seed = 0;
  }

  public PopulationProperties read() {
    double genderBalance = dataPipelineApi.readEstimate("CTM", "gender-balance").doubleValue();
    Distribution populationDistribution =
        dataPipelineApi.readDistribution("CTM", "population-distribution");
    if (isSeeded) {
      populationDistribution.underlyingDistribution().reseedRandomGenerator(seed);
    }

    return ImmutablePopulationProperties.builder()
        .genderBalance(genderBalance)
        .distribution(populationDistribution)
        .build();
  }
}
