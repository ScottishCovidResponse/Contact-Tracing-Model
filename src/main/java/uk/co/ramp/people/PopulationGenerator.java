package uk.co.ramp.people;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.io.types.PopulationOverrides;
import uk.co.ramp.io.types.PopulationProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.utilities.MinMax;

@Service
public class PopulationGenerator {

  private StandardProperties runProperties;
  private PopulationProperties properties;
  private RandomDataGenerator dataGenerator;
  private AgeRetriever ageRetriever;
  private Map<MinMax, Double> ageMap;
  private PopulationOverrides populationOverrides;

  public static Map<VirusStatus, Integer> getCompartmentCounts(Map<Integer, Case> population) {

    Map<VirusStatus, Integer> pop =
        population.values().stream()
            .map(Case::virusStatus)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));

    Stream.of(VirusStatus.values()).forEach(vs -> pop.putIfAbsent(vs, 0));

    return pop;
  }

  @Autowired
  public void setRunProperties(StandardProperties runProperties) {
    this.runProperties = runProperties;
  }

  @Autowired
  public void setProperties(PopulationProperties populationProperties) {
    this.properties = populationProperties;
  }

  @Autowired
  public void setDataGenerator(RandomDataGenerator dataGenerator) {
    this.dataGenerator = dataGenerator;
  }

  @Autowired
  public void setAgeRetriever(AgeRetriever ageRetriever) {
    this.ageRetriever = ageRetriever;
  }

  @Autowired
  public void setPopulationOverrides(PopulationOverrides populationOverrides) {
    this.populationOverrides = populationOverrides;
    ageMap =
        populationOverrides.ageDependentList().stream()
            .collect(
                Collectors.toMap(
                    PopulationOverrides.AgeDependentHealth::range,
                    PopulationOverrides.AgeDependentHealth::modifier));
  }

  public Map<Integer, Case> generate() {
    Map<Integer, Case> population = new HashMap<>();

    for (int i = 0; i < runProperties.populationSize(); i++) {

      int age = ageRetriever.findAge(i);

      double healthModifier = getHealthModifier(age);

      Gender gender =
          dataGenerator.nextUniform(0, 1) > properties.genderBalance() / 2d
              ? Gender.FEMALE
              : Gender.MALE;

      double isolationCompliance =
          populationOverrides.fixedIsolationCompliance().isPresent()
              ? populationOverrides.fixedIsolationCompliance().getAsDouble()
              : dataGenerator.nextUniform(0, 1);

      double reportingCompliance =
          populationOverrides.fixedReportingCompliance().isPresent()
              ? populationOverrides.fixedReportingCompliance().getAsDouble()
              : dataGenerator.nextUniform(0, 1);

      double health = healthModifier * dataGenerator.nextUniform(0, 1);

      boolean hasApp = dataGenerator.nextUniform(0, 1) < properties.appUptake();

      population.put(
          i,
          new Case(
              ImmutableHuman.builder()
                  .id(i)
                  .age(age)
                  .isolationCompliance(isolationCompliance)
                  .reportingCompliance(reportingCompliance)
                  .gender(gender)
                  .health(health)
                  .hasApp(hasApp)
                  .build()));
    }

    return population;
  }

  public double getHealthModifier(int age) {

    return ageMap.entrySet().stream()
        .filter(entry -> entry.getKey().min() <= age && entry.getKey().max() >= age)
        .map(Map.Entry::getValue)
        .findAny()
        .orElse(1d);
  }
}
