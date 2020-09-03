package uk.co.ramp.io.readers;

import uk.co.ramp.io.types.ImmutablePopulationProperties;
import uk.co.ramp.io.types.PopulationProperties;
import uk.ramp.api.StandardApi;
import uk.ramp.distribution.Distribution;

public class PopulationPropertiesReader {
  private final StandardApi dataPipelineApi;

  public PopulationPropertiesReader(StandardApi dataPipelineApi) {
    this.dataPipelineApi = dataPipelineApi;
  }

  public PopulationProperties read() {
    double genderBalance =
        dataPipelineApi.readEstimate("population_parameters", "gender-balance").doubleValue();
    Distribution populationDistribution =
        dataPipelineApi.readDistribution("population_parameters", "population-distribution");
    double appUptake =
        dataPipelineApi.readEstimate("population_parameters", "app-uptake").doubleValue();
    double testCapacity =
        dataPipelineApi.readEstimate("population_parameters", "test-capacity").doubleValue();

    return ImmutablePopulationProperties.builder()
        .genderBalance(genderBalance)
        .distribution(populationDistribution)
        .appUptake(appUptake)
        .testCapacity(testCapacity)
        .build();
  }
}
