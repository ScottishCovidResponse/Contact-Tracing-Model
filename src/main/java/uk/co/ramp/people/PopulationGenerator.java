package uk.co.ramp.people;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.utilities.MinMax;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class PopulationGenerator {

    private final StandardProperties runProperties;
    private final PopulationProperties properties;
    RandomDataGenerator dataGenerator;

    @Autowired
    public PopulationGenerator(final StandardProperties runProperties, final PopulationProperties properties, final RandomDataGenerator dataGenerator) {
        this.runProperties = runProperties;
        this.properties = properties;
        this.dataGenerator = dataGenerator;
    }

    public static Map<VirusStatus, Integer> getCmptCounts(Map<Integer, Case> population) {

        Map<VirusStatus, Integer> pop = population.values().stream()
                .map(Case::status)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));

        Stream.of(VirusStatus.values()).forEach(vs -> pop.putIfAbsent(vs, 0));

        return pop;

    }

    int findAge(Map<Integer, Double> populationDistribution, Map<Integer, MinMax> populationAges) {
        int maxAge = populationAges.values().stream().mapToInt(MinMax::max).max().orElseThrow();
        int[] outcomes = IntStream.rangeClosed(0, maxAge).toArray();
        double[] probabilities = IntStream
                .range(0, populationAges.size())
                .mapToObj(idx -> IntStream
                        .rangeClosed(populationAges.get(idx).min(), populationAges.get(idx).max())
                        .mapToDouble(age -> populationDistribution.get(idx) / (populationAges.get(idx).max() - populationAges.get(idx).min() + 1))
                        .toArray())
                .flatMapToDouble(DoubleStream::of)
                .toArray();

        EnumeratedIntegerDistribution distribution = new EnumeratedIntegerDistribution(dataGenerator.getRandomGenerator(), outcomes, probabilities);
        return distribution.sample();
    }

    public Map<Integer, Case> generate() {
        Map<Integer, Case> population = new HashMap<>();

        for (int i = 0; i < runProperties.populationSize(); i++) {

            int age = findAge(properties.populationDistribution(), properties.populationAges());
            Gender gender = dataGenerator.nextUniform(0, 1) > properties.genderBalance() / 2d ? Gender.FEMALE : Gender.MALE;
            double compliance = dataGenerator.nextUniform(0, 1);
            double health = dataGenerator.nextUniform(0, 1);

            population.put(i,
                    new Case(ImmutableHuman.builder().
                            id(i).
                            age(age).
                            compliance(compliance).
                            gender(gender).
                            health(health).
                            build()));

        }

        return population;
    }
}
