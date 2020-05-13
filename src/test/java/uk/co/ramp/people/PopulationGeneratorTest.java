package uk.co.ramp.people;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.utilities.MinMax;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.*;


public class PopulationGeneratorTest {

    private static final double DELTA = 1e-6;
    private PopulationGenerator populationGenerator;

    @Before
    public void setup() {
        PopulationProperties populationProperties = Mockito.mock(PopulationProperties.class);
        StandardProperties properties = Mockito.mock(StandardProperties.class);
        populationGenerator = new PopulationGenerator(properties, populationProperties);
    }

    @Test
    public void getSEIRCounts() {

        Random rand = new Random();
        int s = rand.nextInt(100);
        int e = rand.nextInt(100);
        int i = rand.nextInt(100);
        int r = rand.nextInt(100);

        Map<Integer, Person> var = new HashMap<>();
        int start = 0;
        int end = s;
        VirusStatus v = SUSCEPTIBLE;
        createPeople(var, start, end, v);

        start += s;
        end += e;
        v = EXPOSED;
        createPeople(var, start, end, v);

        start += e;
        end += i;
        v = INFECTED;
        createPeople(var, start, end, v);

        start += i;
        end += r;
        v = RECOVERED;
        createPeople(var, start, end, v);

        Map<VirusStatus, Integer> result = PopulationGenerator.getSEIRCounts(var);

        Assert.assertEquals(s, result.get(SUSCEPTIBLE).intValue());
        Assert.assertEquals(e, result.get(EXPOSED).intValue());
        Assert.assertEquals(i, result.get(INFECTED).intValue());
        Assert.assertEquals(r, result.get(RECOVERED).intValue());

    }

    //        PopulationGenerator.createCumulative();
    @Test
    public void testCreateCumulative() {


        Random rnd = new Random();
        Map<Integer, Double> var = new HashMap<>();
        Map<Integer, Double> cumulative = new HashMap<>();
        int bins = rnd.nextInt(5) + 5;
        double sum = 0d;
        for (int i = 0; i < bins; i++) {
            double sample = rnd.nextDouble() * 0.2;
            sum += sample;

            if (sum > 1d) {
                var.put(i, sample);
                cumulative.put(i, 1d);
                break;
            } else if (i == bins - 1) {
                var.put(i, 1 - sum);
                cumulative.put(i, 1d);
                break;
            }

            var.put(i, sample);
            cumulative.put(i, sum);

        }

        Map<Integer, Double> result = populationGenerator.createCumulative(var);

        for (int i = 0; i < bins; i++) {
            Assert.assertEquals(cumulative.get(i), result.get(i), DELTA);
        }

    }


    @Test
    public void testFindAgeSimple() {

        Random rnd = new Random();

        Map<Integer, Double> populationDistribution = new HashMap<>();
        Map<Integer, MinMax> ageRanges = new HashMap<>();

        MinMax minMax = new MinMax(0, 10);
        ageRanges.put(0, minMax);
        populationDistribution.put(0, 1d);

        for (int i = 0; i < 100; i++) {
            double a = rnd.nextDouble();
            int age = populationGenerator.findAge(a, populationDistribution, ageRanges);
            Assert.assertTrue(age <= minMax.getMax());
            Assert.assertTrue(age >= minMax.getMin());
        }

        ageRanges.put(1, new MinMax(11, 20));
        populationDistribution.put(0, 0.5d);
        populationDistribution.put(1, 1d);

        for (int i = 0; i < 200; i++) {
            double sample = rnd.nextDouble();
            int age = populationGenerator.findAge(sample, populationDistribution, ageRanges);
            if (sample > 0.5) {
                Assert.assertTrue(age <= ageRanges.get(1).getMax());
                Assert.assertTrue(age >= ageRanges.get(1).getMin());
            } else {
                Assert.assertTrue(age <= ageRanges.get(0).getMax());
                Assert.assertTrue(age >= ageRanges.get(0).getMin());

            }
        }
    }


    @Test
    public void testFindAgeUniform() {

        Random rnd = new Random();

        Map<Integer, Double> ageDistribution = generateAgeDistribution();
        Map<Integer, MinMax> ageRanges = generateAgeRanges();

        for (int i = 0; i < 200; i++) {
            double a = rnd.nextDouble();
            int age = populationGenerator.findAge(a, ageDistribution, ageRanges);
            if (a <= 0.2d) {
                Assert.assertTrue(age <= ageRanges.get(0).getMax());
                Assert.assertTrue(age >= ageRanges.get(0).getMin());
            } else if (a > 0.2d && a < 0.4d) {
                Assert.assertTrue(age <= ageRanges.get(1).getMax());
                Assert.assertTrue(age >= ageRanges.get(1).getMin());
            } else if (a > 0.4d && a < 0.6d) {
                Assert.assertTrue(age <= ageRanges.get(2).getMax());
                Assert.assertTrue(age >= ageRanges.get(2).getMin());
            } else if (a > 0.6d && a < 0.8d) {
                Assert.assertTrue(age <= ageRanges.get(3).getMax());
                Assert.assertTrue(age >= ageRanges.get(3).getMin());
            } else if (a > 0.8d) {
                Assert.assertTrue(age <= ageRanges.get(4).getMax());
                Assert.assertTrue(age >= ageRanges.get(4).getMin());

            }
        }
    }

    private Map<Integer, Double> generateAgeDistribution() {
        Map<Integer, Double> ageDistribution = new HashMap<>();
        ageDistribution.put(0, 0.2d);
        ageDistribution.put(1, 0.4d);
        ageDistribution.put(2, 0.6d);
        ageDistribution.put(3, 0.8d);
        ageDistribution.put(4, 1d);
        return ageDistribution;
    }

    private Map<Integer, MinMax> generateAgeRanges() {
        Map<Integer, MinMax> ageRanges = new HashMap<>();
        ageRanges.put(0, new MinMax(0, 20));
        ageRanges.put(1, new MinMax(20, 39));
        ageRanges.put(2, new MinMax(40, 59));
        ageRanges.put(3, new MinMax(60, 79));
        ageRanges.put(4, new MinMax(80, 99));
        return ageRanges;
    }


    @Test
    public void testGeneratePopulation() {

        Map<Integer, Double> populationDistribution = generateAgeDistribution();
        Map<Integer, MinMax> populationAges = generateAgeRanges();
        double genderBalance = 1.d;
        PopulationProperties populationProperties = new PopulationProperties(populationDistribution, populationAges, genderBalance);

        int populationSize = 10000;
        int timeLimit = 0;
        int infected = 0;
        int seed = 10;
        boolean steadyState = true;

        StandardProperties a = new StandardProperties(populationSize, timeLimit, infected, seed, steadyState);

        populationGenerator = new PopulationGenerator(a, populationProperties);

        Map<Integer, Person> result = populationGenerator.generate();

        int men = 0;
        int women = 0;
        double compliance = 0d;
        double health = 0d;
        for (Person p : result.values()) {
            compliance += p.getCompliance();
            health += p.getHealth();

            if (p.getGender() == Gender.FEMALE) {
                women++;
            } else {
                men++;
            }

        }

        Assert.assertEquals(0.5, compliance / (double) populationSize, 1d / Math.sqrt(populationSize));
        Assert.assertEquals(0.5, health / (double) populationSize, 1d / Math.sqrt(populationSize));

        Assert.assertEquals(populationSize, men + women);

        Assert.assertEquals(0.5, men / (double) populationSize, 1d / Math.sqrt(populationSize));
        Assert.assertEquals(0.5, women / (double) populationSize, 1d / Math.sqrt(populationSize));


    }


    private void createPeople(Map<Integer, Person> var, int start, int end, VirusStatus v) {
        for (int p = start; p < end; p++) {

            Person person = Mockito.mock(Person.class);
            when(person.getStatus()).thenReturn(v);
            var.put(p, person);


        }
    }

}