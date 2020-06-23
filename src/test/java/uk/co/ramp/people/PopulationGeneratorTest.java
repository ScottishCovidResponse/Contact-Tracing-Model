package uk.co.ramp.people;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.*;

import java.io.FileNotFoundException;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.TestUtils;
import uk.co.ramp.io.types.StandardProperties;

public class PopulationGeneratorTest {

  private static final double DELTA = 1e-6;
  private PopulationGenerator populationGenerator;
  private final Random random = TestUtils.getRandom();

  @Before
  public void setup() throws FileNotFoundException {
    populationGenerator = new PopulationGenerator();
    populationGenerator.setProperties(TestUtils.populationProperties());
    populationGenerator.setDataGenerator(TestUtils.dataGenerator());
  }

  @Test
  public void getCompartmentCounts() {

    int s = random.nextInt(100);
    int e = random.nextInt(100);
    int i = random.nextInt(100);
    int r = random.nextInt(100);

    Map<Integer, Case> var = new HashMap<>();
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
    v = PRESYMPTOMATIC;
    createPeople(var, start, end, v);

    start += i;
    end += r;
    v = RECOVERED;
    createPeople(var, start, end, v);

    Map<VirusStatus, Integer> result = PopulationGenerator.getCmptCounts(var);

    Assert.assertEquals(s, result.get(SUSCEPTIBLE).intValue());
    Assert.assertEquals(e, result.get(EXPOSED).intValue());
    Assert.assertEquals(i, result.get(PRESYMPTOMATIC).intValue());
    Assert.assertEquals(r, result.get(RECOVERED).intValue());
  }

  @Test
  public void findAge() {

    int n = 10000;
    List<Integer> ages = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      ages.add(populationGenerator.findAge());
    }

    double sum = ages.stream().mapToInt(Integer::intValue).average().orElseThrow();
    int max = ages.stream().mapToInt(Integer::intValue).max().orElseThrow();
    int min = ages.stream().mapToInt(Integer::intValue).min().orElseThrow();

    Assert.assertEquals(50d, sum, 0.5);
    Assert.assertTrue(max <= 100);
    Assert.assertTrue(max > 80);
    Assert.assertTrue(min < 20);
    Assert.assertTrue(min >= 0);

    long group0 = ages.stream().filter(a -> a < 20).count();
    long group1 = ages.stream().filter(a -> a >= 20 && a < 40).count();
    long group2 = ages.stream().filter(a -> a >= 40 && a < 60).count();
    long group3 = ages.stream().filter(a -> a >= 60 && a < 80).count();
    long group4 = ages.stream().filter(a -> a >= 80 && a < 100).count();

    Assert.assertEquals(0.2, group0 / (double) n, 0.01);
    Assert.assertEquals(0.2, group1 / (double) n, 0.01);
    Assert.assertEquals(0.2, group2 / (double) n, 0.01);
    Assert.assertEquals(0.2, group3 / (double) n, 0.01);
    Assert.assertEquals(0.2, group4 / (double) n, 0.01);
  }

  @Test
  public void generate() {
    int popSize = 10000;
    StandardProperties runSettings = mock(StandardProperties.class);
    when(runSettings.populationSize()).thenReturn(popSize);
    populationGenerator.setRunProperties(runSettings);

    Map<Integer, Case> population = populationGenerator.generate();

    Assert.assertEquals(popSize, population.size());

    double compliance =
        population.values().stream().mapToDouble(Case::compliance).average().orElseThrow();
    double health = population.values().stream().mapToDouble(Case::health).average().orElseThrow();
    double age = population.values().stream().mapToDouble(Case::age).average().orElseThrow();
    double men =
        population.values().stream().map(Case::gender).filter(a -> a.equals(Gender.MALE)).count()
            / (double) popSize;
    double women =
        population.values().stream().map(Case::gender).filter(a -> a.equals(Gender.FEMALE)).count()
            / (double) popSize;

    Assert.assertEquals(0.5, compliance, 0.01);
    Assert.assertEquals(0.5, health, 0.01);
    Assert.assertEquals(50, age, 0.5);
    Assert.assertEquals(0.5, men, 0.01);
    Assert.assertEquals(0.5, women, 0.01);
  }

  private void createPeople(Map<Integer, Case> var, int start, int end, VirusStatus v) {
    for (int p = start; p < end; p++) {

      Case c = mock(Case.class);
      when(c.virusStatus()).thenReturn(v);
      var.put(p, c);
    }
  }
}
