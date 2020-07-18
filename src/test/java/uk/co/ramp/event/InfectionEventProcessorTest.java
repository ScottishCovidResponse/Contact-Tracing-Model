package uk.co.ramp.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.EXPOSED;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;
import static uk.co.ramp.people.VirusStatus.SYMPTOMATIC;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.ramp.AppConfig;
import uk.co.ramp.Population;
import uk.co.ramp.TestConfig;
import uk.co.ramp.TestUtils;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.ImmutableInfectionEvent;
import uk.co.ramp.event.types.InfectionEvent;
import uk.co.ramp.event.types.ProcessedEventResult;
import uk.co.ramp.event.types.VirusEvent;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.Case;

@RunWith(SpringRunner.class)
@DirtiesContext
@Import({TestUtils.class, AppConfig.class, TestConfig.class})
public class InfectionEventProcessorTest {
  private InfectionEventProcessor eventProcessor;
  private DiseaseProperties diseaseProperties;

  private static final double DELTA = 1e-6;;
  @Autowired private StandardProperties properties;
  @Autowired private DistributionSampler distributionSampler;

  private Case thisCase;

  private InfectionEvent event;

  @Before
  public void setUp() throws FileNotFoundException {
    diseaseProperties = TestUtils.diseaseProperties();

    thisCase = mock(Case.class);
    when(thisCase.virusStatus()).thenReturn(SUSCEPTIBLE);
    when(thisCase.health()).thenReturn(-1d);

    Map<Integer, Case> population = new HashMap<>();
    population.put(0, thisCase);

    this.eventProcessor =
        new InfectionEventProcessor(
            new Population(population), properties, diseaseProperties, distributionSampler);

    event =
        ImmutableInfectionEvent.builder()
            .exposedBy(10)
            .exposedTime(5)
            .id(0)
            .nextStatus(EXPOSED)
            .oldStatus(SUSCEPTIBLE)
            .time(3)
            .build();
  }

  @Test
  public void runInfectionEvents() {

    int infector = 0;
    int infectee = 1;

    Case mock0 = mock(Case.class);
    when(mock0.virusStatus()).thenReturn(SUSCEPTIBLE);
    when(mock0.isInfectious()).thenReturn(false);
    when(mock0.id()).thenReturn(infector);

    Case mock1 = mock(Case.class);
    when(mock1.id()).thenReturn(infectee);
    when(mock1.virusStatus()).thenReturn(SYMPTOMATIC);
    when(mock1.isInfectious()).thenReturn(true);

    Map<Integer, Case> population = new HashMap<>();
    population.put(0, mock0);
    population.put(1, mock1);
    ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

    InfectionEvent event =
        ImmutableInfectionEvent.builder()
            .time(0)
            .exposedBy(1)
            .id(0)
            .exposedTime(0)
            .oldStatus(SUSCEPTIBLE)
            .nextStatus(EXPOSED)
            .build();

    ProcessedEventResult processedEventResult = eventProcessor.processEvent(event);

    Assert.assertEquals(1, processedEventResult.newVirusEvents().size());
    Assert.assertEquals(0, processedEventResult.newInfectionEvents().size());
    Assert.assertEquals(0, processedEventResult.newContactEvents().size());
    Assert.assertEquals(0, processedEventResult.newAlertEvents().size());
    Assert.assertEquals(1, processedEventResult.newCompletedInfectionEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedVirusEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedContactEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedAlertEvents().size());
    VirusEvent evnt = processedEventResult.newVirusEvents().get(0);

    Assert.assertEquals(
        diseaseProperties.timeLatent().mean() * properties.timeStepsPerDay(), evnt.time(), DELTA);
    Assert.assertEquals(0, evnt.id());
    Assert.assertEquals(EXPOSED, evnt.oldStatus());
    Assert.assertTrue(EXPOSED.getValidTransitions().contains(evnt.nextStatus()));
  }

  @Test
  public void testInfectionEventExposedBySetInCase() {
    eventProcessor.processEvent(event);
    verify(thisCase).setExposedBy(10);
  }

  @Test
  public void testInfectionEventExposedTimeSetInCase() {
    eventProcessor.processEvent(event);
    verify(thisCase).setExposedTime(5);
  }

  @Test
  public void testInfectionEventVirusStatusSetInCase() {
    eventProcessor.processEvent(event);
    verify(thisCase).setVirusStatus(EXPOSED);
  }

  @Test
  public void testClashingInfectionEvents() {
    InfectionEvent event =
        ImmutableInfectionEvent.builder()
            .time(3)
            .exposedBy(1)
            .id(2)
            .exposedTime(1)
            .oldStatus(SUSCEPTIBLE)
            .nextStatus(EXPOSED)
            .build();

    var population = mock(Population.class);
    when(population.getVirusStatus(eq(2))).thenReturn(EXPOSED);
    when(population.isInfectious(eq(2))).thenReturn(true);
    this.eventProcessor =
        new InfectionEventProcessor(population, properties, diseaseProperties, distributionSampler);

    var processedEvents = eventProcessor.processEvent(event);
    assertThat(processedEvents.newVirusEvents()).isEmpty();
    assertThat(processedEvents.newInfectionEvents()).isEmpty();
    assertThat(processedEvents.newContactEvents()).isEmpty();
    assertThat(processedEvents.newAlertEvents()).isEmpty();
    assertThat(processedEvents.newCompletedInfectionEvents()).isEmpty();
    assertThat(processedEvents.newCompletedVirusEvents()).isEmpty();
    assertThat(processedEvents.newCompletedContactEvents()).isEmpty();
    assertThat(processedEvents.newCompletedAlertEvents()).isEmpty();
    verify(population, times(1)).getVirusStatus(2);
    verify(population, never()).setVirusStatus(2, EXPOSED);
    verifyNoMoreInteractions(population);
  }
}
