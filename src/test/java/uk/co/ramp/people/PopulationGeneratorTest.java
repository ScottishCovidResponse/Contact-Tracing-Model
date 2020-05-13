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

    @Test
    public void testCreateCumulative() {

        Random rnd = new Random();
        Map<Integer, Double> var = new HashMap<>();
        Map<Integer, Double> cumulative = new HashMap<>();

        // create a random number of bins, between 5 and 10
        int bins = rnd.nextInt(5) + 5;
        double sum = 0d;


        for (int i = 0; i < bins; i++) {

            // add a small value on to the end of the last bin
            double sample = rnd.nextDouble() * 0.2;
            sum += sample;

            // if we overflow 1, end here with 1
            // or put 1 in the last place.
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

        // Assert the two methods produce the same result.
        for (int i = 0; i < bins; i++) {
            Assert.assertEquals(cumulative.get(i), result.get(i), DELTA);
        }

    }


    @Test
    public void testFindAgeSimple() {

        Random rnd = new Random();

        Map<Integer, Double> b = new HashMap<>();
        Map<Integer, MinMax> c = new HashMap<>();

        MinMax minMax = new MinMax(0, 10);
        c.put(0, minMax);
        b.put(0, 1d);

        for (int i = 0; i < 100; i++) {
            double a = rnd.nextDouble();
            int age = populationGenerator.findAge(a, b, c);
            Assert.assertTrue(age <= minMax.getMax());
            Assert.assertTrue(age >= minMax.getMin());
        }

        c.put(1, new MinMax(11, 20));
        b.put(0, 0.5d);
        b.put(1, 1d);

        for (int i = 0; i < 200; i++) {
            double a = rnd.nextDouble();
            int age = populationGenerator.findAge(a, b, c);
            if (a > 0.5) {
                Assert.assertTrue(age <= c.get(1).getMax());
                Assert.assertTrue(age >= c.get(1).getMin());
            } else {
                Assert.assertTrue(age <= c.get(0).getMax());
                Assert.assertTrue(age >= c.get(0).getMin());

            }
        }
    }


    @Test
    public void testFindAgeUniform() {

        Random rnd = new Random();

        Map<Integer, Double> b = generateAgeDistribution();
        Map<Integer, MinMax> c = generateAgeRanges();

        for (int i = 0; i < 200; i++) {
            double a = rnd.nextDouble();
            int age = populationGenerator.findAge(a, b, c);
            if (a <= 0.2d) {
                Assert.assertTrue(age <= c.get(0).getMax());
                Assert.assertTrue(age >= c.get(0).getMin());
            } else if (a > 0.2d && a < 0.4d) {
                Assert.assertTrue(age <= c.get(1).getMax());
                Assert.assertTrue(age >= c.get(1).getMin());
            } else if (a > 0.4d && a < 0.6d) {
                Assert.assertTrue(age <= c.get(2).getMax());
                Assert.assertTrue(age >= c.get(2).getMin());
            } else if (a > 0.6d && a < 0.8d) {
                Assert.assertTrue(age <= c.get(3).getMax());
                Assert.assertTrue(age >= c.get(3).getMin());
            } else if (a > 0.8d) {
                Assert.assertTrue(age <= c.get(4).getMax());
                Assert.assertTrue(age >= c.get(4).getMin());

            }
        }
    }

    private Map<Integer, Double> generateAgeDistribution() {
        Map<Integer, Double> b = new HashMap<>();
        b.put(0, 0.2d);
        b.put(1, 0.4d);
        b.put(2, 0.6d);
        b.put(3, 0.8d);
        b.put(4, 1d);
        return b;
    }

    private Map<Integer, MinMax> generateAgeRanges() {
        Map<Integer, MinMax> c = new HashMap<>();
        c.put(0, new MinMax(0, 20));
        c.put(1, new MinMax(20, 39));
        c.put(2, new MinMax(40, 59));
        c.put(3, new MinMax(60, 79));
        c.put(4, new MinMax(80, 99));
        return c;
    }


    @Test
    public void testGeneratePopulation() {

        Map<Integer, Double> populationDistribution = generateAgeDistribution();
        Map<Integer, MinMax> populationAges = generateAgeRanges();
        double genderBalance = 1.d;
        PopulationProperties b = new PopulationProperties(populationDistribution, populationAges, genderBalance);

        int populationSize = 10000;
        int timeLimit = 0;
        int infected = 0;
        int seed = 10;
        boolean steadyState = true;

        StandardProperties a = new StandardProperties(populationSize, timeLimit, infected, seed, steadyState);

        populationGenerator = new PopulationGenerator(a, b);

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