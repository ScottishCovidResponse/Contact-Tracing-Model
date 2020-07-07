package uk.co.ramp.event;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.ramp.*;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.Human;
import uk.co.ramp.policy.alert.TracingPolicyContext;
import uk.co.ramp.policy.isolation.IsolationPolicy;

@RunWith(SpringRunner.class)
@DirtiesContext
@Import({TestUtils.class, AppConfig.class, TestConfig.class, TracingPolicyContext.class})
public class ContactEventProcessorTest {
  @Rule public LogSpy logSpy = new LogSpy();

  private ContactEventProcessor eventProcessor;

  @Before
  public void setUp() throws Exception {
    DiseaseProperties diseaseProperties = TestUtils.diseaseProperties();

    Human human = mock(Human.class);
    when(human.health()).thenReturn(-1d);
    Case aCase = new Case(human);

    Map<Integer, Case> population = new HashMap<>();
    population.put(0, aCase);

    DistributionSampler distributionSampler = mock(DistributionSampler.class);
    IsolationPolicy isolationPolicy = mock(IsolationPolicy.class);

    eventProcessor =
        new ContactEventProcessor(
            new Population(population), diseaseProperties, distributionSampler, isolationPolicy);
  }

  @Test
  public void evaluateContact() {

    int infector = 0;
    int infectee = 1;
    int time = 0;

    Case mock0 = mock(Case.class);
    when(mock0.virusStatus()).thenReturn(SYMPTOMATIC);
    when(mock0.isInfectious()).thenReturn(true);
    when(mock0.id()).thenReturn(infector);

    Case mock1 = mock(Case.class);
    when(mock1.id()).thenReturn(infectee);
    when(mock1.virusStatus()).thenReturn(SUSCEPTIBLE);

    Map<Integer, Case> population = new HashMap<>();
    population.put(0, mock0);
    population.put(1, mock1);
    ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

    ContactEvent contactEvent =
        ImmutableContactEvent.builder()
            .from(infector)
            .to(infectee)
            .time(time)
            .weight(5000)
            .label("")
            .build();

    Optional<InfectionEvent> var = eventProcessor.evaluateContact(contactEvent, 0);

    Assert.assertTrue(var.isPresent());

    InfectionEvent infectionEvent = var.get();
    Assert.assertEquals(infectionEvent.time(), time + 1);
    Assert.assertEquals(infectionEvent.id(), infectee);
    Assert.assertEquals(infectionEvent.nextStatus(), EXPOSED);
  }

  @Test
  public void evaluateExposuresReturn() {

    int infector = 0;
    int infectee = 1;
    int time = 0;

    Case mock0 = mock(Case.class);
    when(mock0.virusStatus()).thenReturn(SYMPTOMATIC);
    when(mock0.isInfectious()).thenReturn(true);
    when(mock0.id()).thenReturn(infector);

    Case mock1 = mock(Case.class);
    when(mock1.id()).thenReturn(infectee);
    when(mock1.virusStatus()).thenReturn(SUSCEPTIBLE);

    Map<Integer, Case> population = new HashMap<>();
    population.put(0, mock0);
    population.put(1, mock1);
    ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

    Optional<InfectionEvent> var = eventProcessor.evaluateExposures(mock0, mock1, 5000, time);

    Assert.assertTrue(var.isPresent());

    InfectionEvent infectionEvent = var.get();
    Assert.assertEquals(infectionEvent.time(), time + 1);
    Assert.assertEquals(infectionEvent.id(), infectee);
    Assert.assertEquals(EXPOSED, infectionEvent.nextStatus());
    Assert.assertThat(logSpy.getOutput(), containsString("DANGER MIX"));
  }

  @Test
  public void evaluateExposuresEmpty() {

    int infector = 0;
    int infectee = 1;
    int time = 0;

    Case mock0 = mock(Case.class);
    when(mock0.virusStatus()).thenReturn(SUSCEPTIBLE);
    when(mock0.isInfectious()).thenReturn(false);
    when(mock0.id()).thenReturn(infector);

    Case mock1 = mock(Case.class);
    when(mock1.id()).thenReturn(infectee);
    when(mock1.virusStatus()).thenReturn(SUSCEPTIBLE);

    Map<Integer, Case> population = new HashMap<>();
    population.put(0, mock0);
    population.put(1, mock1);
    ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

    Optional<InfectionEvent> var = eventProcessor.evaluateExposures(mock0, mock1, 5000, time);

    System.out.println(logSpy.getOutput());
    Assert.assertFalse(var.isPresent());
    Assert.assertTrue(logSpy.getOutput().isEmpty());
  }

  @Test
  public void runContactEvents() {

    int infector = 0;
    int infectee = 1;

    Case mock0 = mock(Case.class);
    when(mock0.virusStatus()).thenReturn(SUSCEPTIBLE);
    when(mock0.isInfectious()).thenReturn(true);
    when(mock0.id()).thenReturn(infector);

    Case mock1 = mock(Case.class);
    when(mock1.id()).thenReturn(infectee);
    when(mock1.virusStatus()).thenReturn(SYMPTOMATIC);
    when(mock1.isInfectious()).thenReturn(true);

    Map<Integer, Case> population = new HashMap<>();
    population.put(0, mock0);
    population.put(1, mock1);

    ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

    ContactEvent event =
        ImmutableContactEvent.builder().time(0).to(0).from(1).weight(50000).label("").build();

    ProcessedEventResult processedEventResult = eventProcessor.processEvent(event);

    Assert.assertEquals(1, processedEventResult.newInfectionEvents().size());
    Assert.assertEquals(0, processedEventResult.newVirusEvents().size());
    Assert.assertEquals(0, processedEventResult.newContactEvents().size());
    Assert.assertEquals(0, processedEventResult.newAlertEvents().size());
    Assert.assertEquals(1, processedEventResult.newCompletedContactEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedAlertEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedInfectionEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedVirusEvents().size());
    InfectionEvent evnt = processedEventResult.newInfectionEvents().get(0);

    Assert.assertEquals(1, evnt.time());
    Assert.assertEquals(0, evnt.id());
    Assert.assertEquals(SUSCEPTIBLE, evnt.oldStatus());
    Assert.assertEquals(EXPOSED, evnt.nextStatus());
  }
}
