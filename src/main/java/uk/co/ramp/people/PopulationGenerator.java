package uk.co.ramp.people;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.utilities.MinMax;

import java.util.*;

import static uk.co.ramp.people.VirusStatus.*;

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

        Map<VirusStatus, Integer> counts = new EnumMap<>(VirusStatus.class);
        counts.put(SUSCEPTIBLE, 0);
        counts.put(EXPOSED, 0);
        counts.put(EXPOSED_2, 0);
        counts.put(INFECTED, 0);
        counts.put(INFECTED_SYMP, 0);
        counts.put(RECOVERED, 0);
        counts.put(DEAD, 0);

        for (Case p : population.values()) {
            counts.merge(p.status(), 1, Integer::sum);
        }

        return counts;
    }

    int findAge(double v, Map<Integer, Double> c, Map<Integer, MinMax> populationAges) {

        int index;

        if (v < c.get(0)) {
            index = 0;
        } else if (v > c.get(0) && v <= c.get(1)) {
            index = 1;
        } else if (v > c.get(1) && v <= c.get(2)) {
            index = 2;
        } else if (v > c.get(2) && v <= c.get(3)) {
            index = 3;
        } else {
            index = 4;
        }

        final double rMin = c.getOrDefault(index - 1, 0d);
        final double rMax = c.get(index);
        final int ageMin = populationAges.get(index).min();
        final int ageMax = populationAges.get(index).max();

        final double distance = (v - rMin) / (rMax - rMin);

        return (int) Math.round((ageMax - ageMin) * distance + ageMin);

    }

    // the population data is provided in non-cumulative form. This creates a cumulative distribution
    Map<Integer, Double> createCumulative(Map<Integer, Double> populationDistribution) {

        List<Double> data = new ArrayList<>();
        Map<Integer, Double> cumulative = new HashMap<>();

        // extract the data in ascending order
        for (Map.Entry<Integer, Double> entry : populationDistribution.entrySet()) {
            data.add(entry.getKey(), populationDistribution.get(entry.getKey()));
        }

        // loop over the data
        double sum = 0d;
        for (int i = 0; i < data.size(); i++) {
            sum += data.get(i);
            if (i == data.size() - 1) {
                sum = 1d;
            }
            cumulative.put(i, sum);
        }

        return cumulative;
    }

    public Map<Integer, Case> generate() {
        Map<Integer, Double> cumulative = createCumulative(properties.populationDistribution());
        Map<Integer, Case> population = new HashMap<>();

        for (int i = 0; i < runProperties.populationSize(); i++) {

            int age = findAge(dataGenerator.nextUniform(0, 1), cumulative, properties.populationAges());
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
