package uk.co.ramp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.*;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.ramp.event.*;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.InitialCaseReader;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.Human;
import uk.co.ramp.policy.alert.TracingPolicyContext;
import uk.co.ramp.policy.isolation.IsolationPolicyContext;

@SuppressWarnings("unchecked")
@DirtiesContext
@RunWith(SpringRunner.class)
@Import({
  TestConfig.class,
  TestUtils.class,
  AppConfig.class,
  IsolationPolicyContext.class,
  TracingPolicyContext.class
})
public class OutbreakTest {

  private final Random random = TestUtils.getRandom();

  @Rule public LogSpy logSpy = new LogSpy();

  @Autowired private StandardProperties standardProperties;

  @Autowired private InitialCaseReader initialCaseReader;

  @Autowired private Outbreak outbreak;

  @Autowired private CompletionEventListGroup eventListGroup;

  @Autowired Population population;

  private DiseaseProperties diseaseProperties;

  public OutbreakTest() throws FileNotFoundException {}

  @Before
  public void setUp() throws FileNotFoundException {
    this.diseaseProperties = TestUtils.diseaseProperties();
  }

  @Test
  @DirtiesContext
  public void runContactDataWithRandoms() {

    int days = 11;
    int popSize = 500;
    double randomInfection = 0.1;
    int initialInfections = 1 + popSize / 10;

    Map<Integer, Case> population = new HashMap<>();

    for (int i = 0; i < popSize; i++) {
      Human human = mock(Human.class);
      when(human.id()).thenReturn(i);
      Case thisCase = new Case(human);
      population.put(i, thisCase);
    }

    Set<Integer> cases = generateTestCases(initialInfections, popSize);
    ReflectionTestUtils.setField(this.initialCaseReader, "cases", cases);

    List<ContactEvent> contacts = createContactRecords(days, population);
    eventListGroup.addNewContactEvents(contacts);

    ReflectionTestUtils.setField(this.diseaseProperties, "randomInfectionRate", randomInfection);
    ReflectionTestUtils.setField(this.population, "population", population);

    outbreak.runContactData(days - 1, randomInfection);

    long sus =
        population.values().stream()
            .map(Case::virusStatus)
            .filter(status -> status == SUSCEPTIBLE)
            .count();

    // We expect this to roughly follow an exp decay, and the error is calibrated in geometric rate
    // space.
    //
    // If r='randomInfection' ratio of people become other status than SUSCEPTIBLE,
    // after T days, remaining population of SUSCEPTIBLE persons should be
    // (1-r)^T = exp(T log(1-r))
    // Hence the first argument of the next line is the actual geometric rate,
    // while the second argument is the assumed one when no contact-based infections happen.
    Assert.assertEquals(
        -Math.log(sus / (double) popSize) / days, -Math.log(1 - randomInfection), 0.05);
  }

  // @Test
  // public void getMostSevere() {
  // // case 1, person 2 worse
  // Case person1 = mock(Case.class);
  // Case person2 = mock(Case.class);
  //
  // when(person1.status()).thenReturn(SUSCEPTIBLE);
  // when(person2.status()).thenReturn(EXPOSED);
  //
  // Case mostSevere = utils.getMostSevere(person1, person2);
  // Assert.assertEquals(person2, mostSevere);
  //
  // // case 2, equal, defaults to person 2
  // when(person2.status()).thenReturn(SUSCEPTIBLE);
  // mostSevere = utils.getMostSevere(person1, person2);
  // Assert.assertEquals(person2, mostSevere);
  //
  // // case 3, equal, defaults to person 2
  // when(person1.status()).thenReturn(PRESYMPTOMATIC);
  // when(person2.status()).thenReturn(PRESYMPTOMATIC);
  //
  // mostSevere = utils.getMostSevere(person1, person2);
  // Assert.assertEquals(person2, mostSevere);
  //
  //
  // // case 2, person 1 worse, behaves correctly
  // when(person1.status()).thenReturn(PRESYMPTOMATIC);
  // when(person2.status()).thenReturn(EXPOSED);
  //
  // mostSevere = utils.getMostSevere(person1, person2);
  // Assert.assertEquals(person1, mostSevere);
  //
  //
  // }

  @Test
  @DirtiesContext
  public void testPropagate() throws FileNotFoundException {
    int popSize = 100;

    DiseaseProperties d = TestUtils.diseaseProperties();
    // outbreak.setDiseaseProperties(d);
    // outbreak.setEventProcessor(eventProcessor);

    ReflectionTestUtils.setField(standardProperties, "timeLimit", 100);
    ReflectionTestUtils.setField(standardProperties, "initialExposures", 10);
    ReflectionTestUtils.setField(standardProperties, "populationSize", popSize);

    Set<Integer> cases = generateTestCases(popSize / 10, popSize);

    ReflectionTestUtils.setField(this.initialCaseReader, "cases", cases);

    Map<Integer, Case> population = new HashMap<>();
    for (int i = 0; i < popSize; i++) {
      Human human = mock(Human.class);
      when(human.id()).thenReturn(i);
      when(human.reportingCompliance()).thenReturn(1d);
      Case thisCase = new Case(human);
      population.put(i, thisCase);
    }

    List<ContactEvent> contacts = createContactRecords(200, population);
    eventListGroup.addNewContactEvents(contacts);

    ReflectionTestUtils.setField(this.population, "population", population);

    long susceptible =
        population.values().stream()
            .map(Case::virusStatus)
            .filter(status -> status == SUSCEPTIBLE)
            .count();

    Map<Integer, CmptRecord> records = outbreak.propagate();

    long susceptiblePost =
        population.values().stream()
            .map(Case::virusStatus)
            .filter(status -> status == SUSCEPTIBLE)
            .count();

    assertThat(population.size()).isGreaterThan(0);
    Assert.assertEquals(records.size(), standardProperties.timeLimit() + 1);
    Assert.assertTrue(susceptiblePost < susceptible);
    Assert.assertThat(
        logSpy.getOutput(),
        containsString("Generated initial outbreak of " + popSize / 10 + " cases"));
    Assert.assertThat(logSpy.getOutput(), containsString("Not all contact data will be used"));
  }

  @Test
  @DirtiesContext
  public void runToCompletionAllContact() throws FileNotFoundException {
    int popSize = 100;
    int infections = 1 + popSize / 10;

    Map<Integer, Case> population = new HashMap<>();

    for (int i = 0; i < popSize; i++) {
      Human h = mock(Human.class);
      when(h.id()).thenReturn(i);
      when(h.reportingCompliance()).thenReturn(1d);
      Case thisCase = new Case(h);
      population.put(i, thisCase);
    }
    Set<Integer> cases = generateTestCases(infections, popSize);
    ReflectionTestUtils.setField(this.initialCaseReader, "cases", cases);

    ReflectionTestUtils.setField(standardProperties, "timeLimit", 100);
    ReflectionTestUtils.setField(standardProperties, "initialExposures", infections);
    ReflectionTestUtils.setField(standardProperties, "populationSize", popSize);
    ReflectionTestUtils.setField(standardProperties, "steadyState", false);

    List<ContactEvent> contacts = createContactRecords(500, population);
    eventListGroup.addNewContactEvents(contacts);

    ReflectionTestUtils.setField(this.population, "population", population);

    long susceptible =
        population.values().stream()
            .map(Case::virusStatus)
            .filter(status -> status == SUSCEPTIBLE)
            .count();
    outbreak.runToCompletion();

    long susceptiblePost =
        population.values().stream()
            .map(Case::virusStatus)
            .filter(status -> status == SUSCEPTIBLE)
            .count();

    assertThat(population.size()).isGreaterThan(0);
    Assert.assertTrue(susceptiblePost < susceptible);
    Assert.assertThat(logSpy.getOutput(), containsString("Not all contact data will be used"));
  }

  @Test
  @DirtiesContext
  public void runToCompletionSmallContact() {
    int popSize = 100;
    int infections = 1 + popSize / 10;

    Map<Integer, Case> population = new HashMap<>();

    for (int i = 0; i < popSize; i++) {
      Human h = mock(Human.class);
      when(h.id()).thenReturn(i);
      when(h.reportingCompliance()).thenReturn(1d);
      Case thisCase = new Case(h);
      population.put(i, thisCase);
    }
    Set<Integer> cases = generateTestCases(infections, popSize);
    ReflectionTestUtils.setField(this.initialCaseReader, "cases", cases);

    List<ContactEvent> contacts = createContactRecords(5, population);
    eventListGroup.addNewContactEvents(contacts);

    ReflectionTestUtils.setField(this.population, "population", population);

    long susceptible =
        population.values().stream()
            .map(Case::virusStatus)
            .filter(status -> status == SUSCEPTIBLE)
            .count();
    outbreak.runToCompletion();

    long susceptiblePost =
        population.values().stream()
            .map(Case::virusStatus)
            .filter(status -> status == SUSCEPTIBLE)
            .count();
    assertThat(population.size()).isGreaterThan(0);
    Assert.assertTrue(susceptiblePost < susceptible);
    Assert.assertThat(
        logSpy.getOutput(),
        containsString("There are no active cases and the random infection rate is zero."));
    Assert.assertThat(logSpy.getOutput(), containsString("Exiting as solution is stable."));
  }

  @Test
  public void runContactDataWithoutRandoms() {

    int days = 11;
    int popSize = 500;
    double randomInfection = 0.0;

    Map<Integer, Case> population = new HashMap<>();

    for (int i = 0; i < popSize; i++) {
      Human human = mock(Human.class);
      Case thisCase = new Case(human);
      population.put(i, thisCase);
    }

    ReflectionTestUtils.setField(this.initialCaseReader, "cases", Set.of());

    List<ContactEvent> contacts = createContactRecords(days, population);
    eventListGroup.addNewContactEvents(contacts);

    ReflectionTestUtils.setField(this.population, "population", population);

    outbreak.runContactData(days - 1, randomInfection);

    Assert.assertThat(
        logSpy.getOutput(),
        containsString("There are no active cases and the random infection rate is zero."));
    Assert.assertThat(logSpy.getOutput(), containsString("Exiting as solution is stable."));
  }

  private List<ContactEvent> createContactRecords(int days, Map<Integer, Case> population) {
    List<ContactEvent> contacts = new ArrayList<>();
    for (int i = 0; i < days; i++) {

      List<ContactEvent> dailyContacts = new ArrayList<>();
      for (int j = 0; j < random.nextInt(population.size()); j++) {

        int personA = random.nextInt(population.size());
        int personB = random.nextInt(population.size());
        int weight = random.nextInt(100);

        dailyContacts.add(
            ImmutableContactEvent.builder()
                .from(personA)
                .to(personB)
                .label("")
                .weight(weight)
                .time(i)
                .build());
      }
      contacts.addAll(dailyContacts);
    }
    return contacts;
  }

  private Set<Integer> generateTestCases(int numCases, int popSize) {
    Set<Integer> cases = new HashSet<>();
    while (cases.size() < numCases) {
      cases.add(random.nextInt(popSize - 1));
    }
    return cases;
  }
}
