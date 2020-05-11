package uk.co.ramp.people;

import org.apache.commons.math3.random.RandomDataGenerator;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.utilities.MinMax;
import uk.co.ramp.utilities.RandomSingleton;

import java.util.*;

public class PopulationGenerator {

    private PopulationGenerator() {
        // hidden constructor
    }

    public static Map<Integer, Person> generate(StandardProperties runProperties, PopulationProperties properties) {
        RandomDataGenerator r = RandomSingleton.getInstance(runProperties.getSeed());
        Map<Integer, Double> cumulative = createCumulative(properties.getPopulationDistribution());
        Map<Integer, Person> population = new HashMap<>();
        for (int i = 0; i < runProperties.getPopulationSize(); i++) {

            int age = findAge(r.nextUniform(0, 1), cumulative, properties.getPopulationAges());
            Gender g = r.nextUniform(0, 1) > properties.getGenderBalance() / 2d ? Gender.FEMALE : Gender.MALE;
            double compliance = r.nextGaussian(0.5, 0.5);
            double health = r.nextGaussian(0.5, 0.5);

            population.put(i, new Person(i, age, g, compliance, health));

        }

        return population;
    }

    static int findAge(double v, Map<Integer, Double> c, Map<Integer, MinMax> populationAges) {

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
        final int ageMin = populationAges.get(index).getMin();
        final int ageMax = populationAges.get(index).getMax();

        final double distance = (v - rMin) / (rMax - rMin);

        return (int) Math.round((ageMax - ageMin) * distance + ageMin);

    }


    static Map<Integer, Double> createCumulative(Map<Integer, Double> populationDistribution) {

        List<Double> data = new ArrayList<>();

        Map<Integer, Double> cumulative = new HashMap<>();

        for (Map.Entry<Integer, Double> entry : populationDistribution.entrySet()) {
            data.add(entry.getKey(), populationDistribution.get(entry.getKey()));
        }

        for (int i = 0; i < data.size(); i++) {
            double sum = 0d;
            if (i < data.size() - 1) {
                sum += data.get(i);
                for (int j = 0; j < i; j++) {
                    sum += data.get(j);
                }
            } else if (i == data.size() - 1) {
                sum = 1d;
            }
            cumulative.put(i, sum);
        }

        return cumulative;
    }

    public static Map<VirusStatus, Integer> getSEIRCounts(Map<Integer, Person> population) {

        int s = 0;
        int e = 0;
        int i = 0;
        int r = 0;


        for (Person p : population.values()) {
            switch (p.getStatus()) {
                case SUSCEPTIBLE:
                    s++;
                    break;
                case EXPOSED:
                    e++;
                    break;
                case INFECTED:
                    i++;
                    break;
                case RECOVERED:
                    r++;
                    break;
            }

        }

        Map<VirusStatus, Integer> counts = new EnumMap<>(VirusStatus.class);

        counts.put(VirusStatus.SUSCEPTIBLE, s);
        counts.put(VirusStatus.EXPOSED, e);
        counts.put(VirusStatus.INFECTED, i);
        counts.put(VirusStatus.RECOVERED, r);

        return counts;
    }
}
