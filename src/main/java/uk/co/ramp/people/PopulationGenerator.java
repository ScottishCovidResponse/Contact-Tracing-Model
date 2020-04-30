package uk.co.ramp.people;

import uk.co.ramp.RandomSingleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PopulationGenerator {

    private static final double genderBalance = 0.99; // males/female


    static double c1;
    static double c2;
    static double c3;
    static double c4;
    static double c5;

    static {

        double p1 = 0.1759;
        double p2 = 0.1171;
        double p3 = 0.4029;
        double p4 = 0.1222;
        double p5 = 0.1819;

        c1 = p1;
        c2 = c1 + p2;
        c3 = c2 + p3;
        c4 = c3 + p4;
        c5 = c4 + p5;

    }


    public static Map<Integer, Person> generate(int popSize) {
        Random r = RandomSingleton.getInstance(0);
        Map<Integer, Person> population = new HashMap<>();
        for (int i = 0; i < popSize; i++) {

            double rand = r.nextDouble();
            int age = findAge(rand);

            rand = r.nextDouble();
            Gender g = rand > genderBalance / 2d ? Gender.FEMALE : Gender.MALE;
            double compliance = r.nextGaussian();
            double health = r.nextGaussian();

            population.put(i, new Person(i, age, g, compliance, health));

        }

        return population;

    }

    private static int findAge(double v) {

        int ageMin;
        int ageMax;
        double rMin;
        double rMax;

        if (v < c1) {
            rMin = 0;
            rMax = c1;
            ageMin = 0;
            ageMax = 14;
        } else if (v > c1 && v <= c2) {
            rMin = c1;
            rMax = c2;
            ageMin = 15;
            ageMax = 24;
        } else if (v > c2 && v <= c3) {
            rMin = c2;
            rMax = c3;
            ageMin = 25;
            ageMax = 54;
        } else if (v > c3 && v <= c4) {
            rMin = c3;
            rMax = c4;
            ageMin = 55;
            ageMax = 64;
        } else {
            rMin = c4;
            rMax = c5;
            ageMin = 65;
            ageMax = 90;
        }

        double distance = (v - rMin) / (rMax - rMin);

        return (int) Math.round((ageMax - ageMin) * distance + ageMin);
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

        Map<VirusStatus, Integer> counts = new HashMap<>();

        counts.put(VirusStatus.SUSCEPTIBLE, s);
        counts.put(VirusStatus.EXPOSED, e);
        counts.put(VirusStatus.INFECTED, i);
        counts.put(VirusStatus.RECOVERED, r);


        return counts;
    }
}
