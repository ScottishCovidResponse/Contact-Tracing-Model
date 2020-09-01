package uk.co.ramp.event;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.ramp.LogSpy;
import uk.co.ramp.Population;
import uk.co.ramp.TestUtils;
import uk.co.ramp.distribution.BoundedDistribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.CommonVirusEvent;
import uk.co.ramp.event.types.Event;
import uk.co.ramp.event.types.ImmutableVirusEvent;
import uk.co.ramp.event.types.ProcessedEventResult;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.Human;
import uk.co.ramp.people.VirusStatus;
import uk.ramp.distribution.Distribution;
import uk.ramp.distribution.ImmutableDistribution;

public class CommonVirusEventProcessorTest {
  @Rule public LogSpy logSpy = new LogSpy();
  public DistributionSampler distributionSampler;
  private CommonVirusEventProcessor<Event> eventProcessor;
  private DiseaseProperties diseaseProperties;
  private StandardProperties properties;

  @Before
  public void setUp() throws Exception {
    diseaseProperties = TestUtils.diseaseProperties();
    distributionSampler = mock(DistributionSampler.class);
    when(distributionSampler.uniformBetweenZeroAndOne()).thenReturn(0.5d);
    properties = mock(StandardProperties.class);
    when(properties.timeStepsPerDay()).thenReturn(10);
    Human human = mock(Human.class);
    when(human.health()).thenReturn(-1d);
    Case aCase = new Case(human);

    Map<Integer, Case> population = new HashMap<>();
    population.put(0, aCase);

    eventProcessor =
        new CommonVirusEventProcessor<>(
            new Population(population), properties, diseaseProperties, distributionSampler) {
          @Override
          public ProcessedEventResult processEvent(Event event) {
            throw new UnsupportedOperationException();
          }
        };
  }

  @Test
  public void determineNextStatus() {
    CommonVirusEvent commonVirusEvent =
        ImmutableVirusEvent.builder()
            .id(0)
            .oldStatus(SUSCEPTIBLE)
            .nextStatus(EXPOSED)
            .time(1)
            .build();
    VirusStatus var = eventProcessor.determineNextStatus(commonVirusEvent);

    Assert.assertTrue(EXPOSED.getValidTransitions().contains(var));

    commonVirusEvent =
        ImmutableVirusEvent.builder()
            .id(0)
            .oldStatus(EXPOSED)
            .nextStatus(PRESYMPTOMATIC)
            .time(1)
            .build();
    var = eventProcessor.determineNextStatus(commonVirusEvent);

    Assert.assertTrue(PRESYMPTOMATIC.getValidTransitions().contains(var));

    commonVirusEvent =
        ImmutableVirusEvent.builder()
            .id(0)
            .oldStatus(SYMPTOMATIC)
            .nextStatus(SEVERELY_SYMPTOMATIC)
            .time(1)
            .build();
    var = eventProcessor.determineNextStatus(commonVirusEvent);

    Assert.assertTrue(SEVERELY_SYMPTOMATIC.getValidTransitions().contains(var));
  }

  private BoundedDistribution createMockBoundedDistribution(int mean, int max) {
    var boundedDistribution = mock(BoundedDistribution.class);
    Distribution distribution =
        ImmutableDistribution.builder()
            .internalType(Distribution.DistributionType.empirical)
            .internalScale(mean)
            .empiricalSamples(List.of(mean))
            .rng(TestUtils.dataGenerator().getRandomGenerator())
            .build();
    when(boundedDistribution.distribution()).thenReturn(distribution);
    when(boundedDistribution.getDistributionValue()).thenReturn(mean);
    when(boundedDistribution.max()).thenReturn(Double.valueOf(max));
    return boundedDistribution;
  }

  @Test
  public void timeInCompartment() {
    diseaseProperties = mock(DiseaseProperties.class);
    distributionSampler = new DistributionSampler(TestUtils.dataGenerator());
    int i = 0;

    var dist = createMockBoundedDistribution(++i, ++i);
    when(diseaseProperties.timeLatent()).thenReturn(dist);
    dist = createMockBoundedDistribution(++i, ++i);
    when(diseaseProperties.timeRecoveryAsymp()).thenReturn(dist);
    dist = createMockBoundedDistribution(++i, ++i);
    when(diseaseProperties.timeRecoverySymp()).thenReturn(dist);
    dist = createMockBoundedDistribution(++i, ++i);
    when(diseaseProperties.timeRecoverySev()).thenReturn(dist);
    dist = createMockBoundedDistribution(++i, ++i);
    when(diseaseProperties.timeSymptomsOnset()).thenReturn(dist);
    dist = createMockBoundedDistribution(++i, ++i);
    when(diseaseProperties.timeDecline()).thenReturn(dist);
    dist = createMockBoundedDistribution(++i, ++i);
    when(diseaseProperties.timeDeath()).thenReturn(dist);

    ReflectionTestUtils.setField(eventProcessor, "diseaseProperties", diseaseProperties);
    ReflectionTestUtils.setField(eventProcessor, "distributionSampler", distributionSampler);

    final double delta = 1e-6;

    Assert.assertEquals(
        diseaseProperties.timeLatent().getDistributionValue(),
        eventProcessor.timeInCompartment(EXPOSED, ASYMPTOMATIC),
        delta);
    Assert.assertEquals(
        diseaseProperties.timeRecoveryAsymp().getDistributionValue(),
        eventProcessor.timeInCompartment(ASYMPTOMATIC, RECOVERED),
        delta);
    Assert.assertEquals(
        diseaseProperties.timeLatent().getDistributionValue(),
        eventProcessor.timeInCompartment(EXPOSED, PRESYMPTOMATIC),
        delta);
    Assert.assertEquals(
        diseaseProperties.timeSymptomsOnset().getDistributionValue(),
        eventProcessor.timeInCompartment(PRESYMPTOMATIC, SYMPTOMATIC),
        delta);
    Assert.assertEquals(
        diseaseProperties.timeRecoverySymp().getDistributionValue(),
        eventProcessor.timeInCompartment(SYMPTOMATIC, RECOVERED),
        delta);
    Assert.assertEquals(
        diseaseProperties.timeDecline().getDistributionValue(),
        eventProcessor.timeInCompartment(SYMPTOMATIC, SEVERELY_SYMPTOMATIC),
        delta);
    Assert.assertEquals(
        diseaseProperties.timeRecoverySev().getDistributionValue(),
        eventProcessor.timeInCompartment(SEVERELY_SYMPTOMATIC, RECOVERED),
        delta);
    Assert.assertEquals(
        diseaseProperties.timeDeath().getDistributionValue(),
        eventProcessor.timeInCompartment(SEVERELY_SYMPTOMATIC, DEAD),
        delta);
  }

  @Test
  public void timeCompartmentEdge() {
    try {
      eventProcessor.timeInCompartment(SUSCEPTIBLE, RECOVERED);
    } catch (RuntimeException e) {
      Assert.assertThat(logSpy.getOutput(), containsString("Unexpected Virus statuses"));
      Assert.assertThat(logSpy.getOutput(), containsString("SUSCEPTIBLE"));
      Assert.assertThat(logSpy.getOutput(), containsString("RECOVERED"));
    }
  }

  @Test
  public void determineInfection() {
    CommonVirusEvent event = mock(CommonVirusEvent.class);
    when(event.nextStatus()).thenReturn(EXPOSED);

    VirusStatus out = eventProcessor.determineInfection(event);
    Assert.assertEquals(PRESYMPTOMATIC, out);

    Human human = mock(Human.class);
    when(human.health()).thenReturn(2d);

    Case aCase = new Case(human);
    Map<Integer, Case> population = new HashMap<>();
    population.put(0, aCase);

    ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

    out = eventProcessor.determineInfection(event);
    Assert.assertEquals(ASYMPTOMATIC, out);
  }

  @Test
  public void determineSeverity() {
    Human human = mock(Human.class);
    when(human.health()).thenReturn(-1d);

    Case aCase = new Case(human);
    Map<Integer, Case> population = new HashMap<>();
    population.put(0, aCase);

    ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

    CommonVirusEvent event = mock(CommonVirusEvent.class);
    when(event.nextStatus()).thenReturn(SYMPTOMATIC);

    VirusStatus out = eventProcessor.determineSeverity(event);
    Assert.assertEquals(SEVERELY_SYMPTOMATIC, out);

    when(human.health()).thenReturn(2d);
    population.put(0, aCase);

    ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

    out = eventProcessor.determineSeverity(event);
    Assert.assertEquals(RECOVERED, out);
  }

  @Test
  public void determineOutcome() {
    Human human = mock(Human.class);
    when(human.health()).thenReturn(-1d);
    Case aCase = new Case(human);

    Map<Integer, Case> population = new HashMap<>();
    population.put(0, aCase);

    ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

    CommonVirusEvent event = mock(CommonVirusEvent.class);
    when(event.nextStatus()).thenReturn(SEVERELY_SYMPTOMATIC);

    VirusStatus out = eventProcessor.determineOutcome(event);
    Assert.assertEquals(DEAD, out);

    when(human.health()).thenReturn(2d);
    population.put(0, aCase);

    ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

    out = eventProcessor.determineOutcome(event);
    Assert.assertEquals(RECOVERED, out);
  }
}
