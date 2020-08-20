package uk.co.ramp.people;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.assertj.core.data.Offset;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.TestUtils;
import uk.co.ramp.io.types.PopulationProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.utilities.MinMax;

public class PopulationGeneratorTest {

  private PopulationGenerator populationGenerator;
  private final Random random = TestUtils.getRandom();

  @Before
  public void setup() throws IOException {
    populationGenerator = new PopulationGenerator();
    populationGenerator.setProperties(TestUtils.populationProperties());
    populationGenerator.setDataGenerator(TestUtils.dataGenerator());
    populationGenerator.setAgeRetriever(TestUtils.ageRetriever());
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
  public void generate() {
    int popSize = 10000;
    StandardProperties runSettings = mock(StandardProperties.class);
    when(runSettings.populationSize()).thenReturn(popSize);
    populationGenerator.setRunProperties(runSettings);

    Map<Integer, Case> population = populationGenerator.generate();

    Assert.assertEquals(popSize, population.size());

    double compliance =
        population.values().stream().mapToDouble(Case::isolationCompliance).average().orElseThrow();
    double health = population.values().stream().mapToDouble(Case::health).average().orElseThrow();
    double age = population.values().stream().mapToDouble(Case::age).average().orElseThrow();
    double men =
        population.values().stream().map(Case::gender).filter(a -> a.equals(Gender.MALE)).count()
            / (double) popSize;
    double women =
        population.values().stream().map(Case::gender).filter(a -> a.equals(Gender.FEMALE)).count()
            / (double) popSize;
    double healthModifier = populationGenerator.getHealthModifier(50);

    double hasApp = population.values().stream().filter(Case::hasApp).count() / (double) popSize;

    System.out.println(hasApp);

    Assert.assertEquals(0.5, compliance, 0.01);
    Assert.assertEquals(0.5 * healthModifier, health, 0.01);
    Assert.assertEquals(50, age, 0.5);
    Assert.assertEquals(0.5, men, 0.01);
    Assert.assertEquals(0.5, women, 0.01);
    Assert.assertEquals(0.7, hasApp, 0.01);
  }

  @Test
  public void getHealthModifier() throws FileNotFoundException {
    PopulationProperties populationProperties = TestUtils.populationProperties();

    double max =
        populationProperties.populationAges().values().stream()
            .mapToDouble(MinMax::max)
            .max()
            .orElse(100);
    double min =
        populationProperties.populationAges().values().stream()
            .mapToDouble(MinMax::min)
            .min()
            .orElse(0);

    Map<Integer, Double> expected = new HashMap<>();
    expected.put(0, 0.9d);
    expected.put(10, 0.9d);
    expected.put(20, 0.8d);
    expected.put(30, 0.8d);
    expected.put(40, 0.6d);
    expected.put(50, 0.6d);
    expected.put(60, 0.4d);
    expected.put(70, 0.4d);
    expected.put(80, 0.2d);
    expected.put(90, 0.2d);

    int low = (int) (min / 10d) - 2;
    int high = (int) (max / 10d) + 2;

    for (int i = low; i < high; i++) {
      int age = i * 10;
      double modifier = populationGenerator.getHealthModifier(age);

      if (age >= max || age < min) {
        assertThat(modifier).isCloseTo(1d, Offset.offset(DELTA));
      } else {
        assertThat(modifier).isCloseTo(expected.get(age), Offset.offset(DELTA));
      }
    }
  }

  private void createPeople(Map<Integer, Case> var, int start, int end, VirusStatus v) {
    for (int p = start; p < end; p++) {

      Case c = mock(Case.class);
      when(c.virusStatus()).thenReturn(v);
      var.put(p, c);
    }
  }
}
