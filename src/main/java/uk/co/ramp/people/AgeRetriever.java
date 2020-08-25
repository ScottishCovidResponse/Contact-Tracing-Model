package uk.co.ramp.people;

import java.util.Map;
import java.util.Optional;
import uk.co.ramp.io.types.PopulationProperties;

public class AgeRetriever {
  private final PopulationProperties properties;
  private final Map<Integer, Integer> inputAgesData;

  public AgeRetriever(PopulationProperties properties, Map<Integer, Integer> inputAgesData) {
    this.properties = properties;
    this.inputAgesData = inputAgesData;
  }

  int findAge(int id) {
    return Optional.ofNullable(inputAgesData.get(id)).orElse(randomAgeUsingDistribution());
  }

  int randomAgeUsingDistribution() {
    return (int) properties.distribution().underlyingDistribution().sample();
  }
}
