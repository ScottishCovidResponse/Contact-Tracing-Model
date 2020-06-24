package uk.co.ramp.event;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.*;
import static uk.co.ramp.people.VirusStatus.SEVERELY_SYMPTOMATIC;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.ramp.*;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.distribution.ProgressionDistribution;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.Human;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.policy.alert.AlertPolicyContext;
import uk.co.ramp.utilities.ImmutableMeanMax;

@RunWith(SpringRunner.class)
@DirtiesContext
@Import({TestUtils.class, AppConfig.class, TestConfig.class, AlertPolicyContext.class})
public class CommonVirusEventProcessorTest {
  @Rule public LogSpy logSpy = new LogSpy();

  private CommonVirusEventProcessor<Event> eventProcessor;

  private DiseaseProperties diseaseProperties;

  @Autowired public DistributionSampler distributionSampler;

  @Before
  public void setUp() throws Exception {
    diseaseProperties = TestUtils.diseaseProperties();

    Human human = mock(Human.class);
    when(human.health()).thenReturn(-1d);
    Case aCase = new Case(human);

    Map<Integer, Case> population = new HashMap<>();
    population.put(0, aCase);

    eventProcessor =
        new CommonVirusEventProcessor<>(
            new Population(population), diseaseProperties, distributionSampler) {
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

  @Test
  public void timeInCompartment() {
    diseaseProperties = Mockito.mock(DiseaseProperties.class);

    int i = 0;

    when(diseaseProperties.progressionDistribution()).thenReturn(ProgressionDistribution.FLAT);
    when(diseaseProperties.timeLatent())
        .thenReturn(ImmutableMeanMax.builder().mean(++i).max(++i).build());
    when(diseaseProperties.timeRecoveryAsymp())
        .thenReturn(ImmutableMeanMax.builder().mean(++i).max(++i).build());
    when(diseaseProperties.timeRecoverySymp())
        .thenReturn(ImmutableMeanMax.builder().mean(++i).max(++i).build());
    when(diseaseProperties.timeRecoverySev())
        .thenReturn(ImmutableMeanMax.builder().mean(++i).max(++i).build());
    when(diseaseProperties.timeSymptomsOnset())
        .thenReturn(ImmutableMeanMax.builder().mean(++i).max(++i).build());
    when(diseaseProperties.timeDecline())
        .thenReturn(ImmutableMeanMax.builder().mean(++i).max(++i).build());
    when(diseaseProperties.timeDeath())
        .thenReturn(ImmutableMeanMax.builder().mean(++i).max(++i).build());

    ReflectionTestUtils.setField(eventProcessor, "diseaseProperties", diseaseProperties);

    Assert.assertEquals(
        diseaseProperties.timeLatent().mean(),
        eventProcessor.timeInCompartment(EXPOSED, ASYMPTOMATIC));
    Assert.assertEquals(
        diseaseProperties.timeRecoveryAsymp().mean(),
        eventProcessor.timeInCompartment(ASYMPTOMATIC, RECOVERED));
    Assert.assertEquals(
        diseaseProperties.timeLatent().mean(),
        eventProcessor.timeInCompartment(EXPOSED, PRESYMPTOMATIC));
    Assert.assertEquals(
        diseaseProperties.timeSymptomsOnset().mean(),
        eventProcessor.timeInCompartment(PRESYMPTOMATIC, SYMPTOMATIC));
    Assert.assertEquals(
        diseaseProperties.timeRecoverySymp().mean(),
        eventProcessor.timeInCompartment(SYMPTOMATIC, RECOVERED));
    Assert.assertEquals(
        diseaseProperties.timeDecline().mean(),
        eventProcessor.timeInCompartment(SYMPTOMATIC, SEVERELY_SYMPTOMATIC));
    Assert.assertEquals(
        diseaseProperties.timeRecoverySev().mean(),
        eventProcessor.timeInCompartment(SEVERELY_SYMPTOMATIC, RECOVERED));
    Assert.assertEquals(
        diseaseProperties.timeDeath().mean(),
        eventProcessor.timeInCompartment(SEVERELY_SYMPTOMATIC, DEAD));
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
