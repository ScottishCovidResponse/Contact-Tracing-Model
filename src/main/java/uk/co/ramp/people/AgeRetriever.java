package uk.co.ramp.people;

import java.util.Map;
import java.util.Optional;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;
import uk.co.ramp.io.types.PopulationProperties;
import uk.co.ramp.utilities.MinMax;

public class AgeRetriever {
  private final PopulationProperties properties;
  private final RandomDataGenerator dataGenerator;
  private final Map<Integer, Integer> inputAgesData;

  public AgeRetriever(
      PopulationProperties properties,
      RandomDataGenerator dataGenerator,
      Map<Integer, Integer> inputAgesData) {
    this.properties = properties;
    this.dataGenerator = dataGenerator;
    this.inputAgesData = inputAgesData;
  }

  int findAge(int id) {
    return Optional.ofNullable(inputAgesData.get(id)).orElse(randomAgeUsingDistribution());
  }

  int randomAgeUsingDistribution() {
    Map<Integer, Double> populationDistribution = properties.populationDistribution();
    Map<Integer, MinMax> populationAges = properties.populationAges();

    int maxAge = populationAges.values().stream().mapToInt(MinMax::max).max().orElseThrow();
    int[] outcomes = IntStream.rangeClosed(0, maxAge).toArray();
    double[] probabilities =
        IntStream.range(0, populationAges.size())
            .mapToObj(
                idx ->
                    IntStream.rangeClosed(
                            populationAges.get(idx).min(), populationAges.get(idx).max())
                        .mapToDouble(
                            age ->
                                populationDistribution.get(idx)
                                    / (populationAges.get(idx).max()
                                        - populationAges.get(idx).min()
                                        + 1))
                        .toArray())
            .flatMapToDouble(DoubleStream::of)
            .toArray();

    EnumeratedIntegerDistribution distribution =
        new EnumeratedIntegerDistribution(
            dataGenerator.getRandomGenerator(), outcomes, probabilities);
    return distribution.sample();
  }
}
