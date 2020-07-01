package uk.co.ramp.people;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.io.types.PopulationProperties;
import uk.co.ramp.io.types.StandardProperties;

@Service
public class PopulationGenerator {

  private StandardProperties runProperties;
  private PopulationProperties properties;
  private RandomDataGenerator dataGenerator;
  private AgeRetriever ageRetriever;

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

  public static Map<VirusStatus, Integer> getCmptCounts(Map<Integer, Case> population) {

    Map<VirusStatus, Integer> pop =
        population.values().stream()
            .map(Case::virusStatus)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));

    Stream.of(VirusStatus.values()).forEach(vs -> pop.putIfAbsent(vs, 0));

    return pop;
  }

  public Map<Integer, Case> generate() {
    Map<Integer, Case> population = new HashMap<>();

    for (int i = 0; i < runProperties.populationSize(); i++) {

      int age = ageRetriever.findAge(i);

      Gender gender =
          dataGenerator.nextUniform(0, 1) > properties.genderBalance() / 2d
              ? Gender.FEMALE
              : Gender.MALE;
      double compliance = dataGenerator.nextUniform(0, 1);
      double health = dataGenerator.nextUniform(0, 1);

      population.put(
          i,
          new Case(
              ImmutableHuman.builder()
                  .id(i)
                  .age(age)
                  .compliance(compliance)
                  .gender(gender)
                  .health(health)
                  .build()));
    }

    return population;
  }
}
