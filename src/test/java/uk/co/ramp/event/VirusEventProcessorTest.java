package uk.co.ramp.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.AlertStatus.REQUESTED_TEST;
import static uk.co.ramp.people.VirusStatus.PRESYMPTOMATIC;
import static uk.co.ramp.people.VirusStatus.SYMPTOMATIC;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
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
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.policy.alert.AlertChecker;
import uk.co.ramp.policy.alert.TracingPolicyContext;

@RunWith(SpringRunner.class)
@DirtiesContext
@Import({TestUtils.class, AppConfig.class, TestConfig.class, TracingPolicyContext.class})
public class VirusEventProcessorTest {
  private VirusEventProcessor eventProcessor;
  private DiseaseProperties diseaseProperties;
  private Population population;
  private final AlertEvent alertEvent =
      ImmutableAlertEvent.builder()
          .id(0)
          .oldStatus(NONE)
          .nextStatus(REQUESTED_TEST)
          .time(2)
          .build();

  @Autowired private StandardProperties properties;
  @Autowired DistributionSampler distributionSampler;

  @Before
  public void setUp() throws FileNotFoundException {
    diseaseProperties = TestUtils.diseaseProperties();
    population = mock(Population.class);

    AlertChecker alertChecker = mock(AlertChecker.class);
    when(alertChecker.checkForAlert(eq(0), eq(NONE), eq(SYMPTOMATIC), eq(1)))
        .thenReturn(Stream.of(alertEvent));

    this.eventProcessor =
        new VirusEventProcessor(
            population, properties, diseaseProperties, distributionSampler, alertChecker);
  }

  @Test
  public void checkForAlert() {

    VirusEvent event =
        ImmutableVirusEvent.builder()
            .time(0)
            .id(0)
            .oldStatus(PRESYMPTOMATIC)
            .nextStatus(SYMPTOMATIC)
            .build();

    when(population.getAlertStatus(eq(0))).thenReturn(NONE);
    when(population.getVirusStatus(eq(0))).thenReturn(PRESYMPTOMATIC);
    assertThat(eventProcessor.checkForAlert(event)).isEmpty();

    event = ImmutableVirusEvent.builder().from(event).time(1).build();
    when(population.getAlertStatus(eq(0))).thenReturn(NONE);
    when(population.getVirusStatus(eq(0))).thenReturn(SYMPTOMATIC);
    assertThat(eventProcessor.checkForAlert(event)).containsExactly(alertEvent);
  }

  @Test
  public void runVirusEventsCompliant() {

    int infector = 0;

    Case mock0 = mock(Case.class);
    when(mock0.virusStatus()).thenReturn(PRESYMPTOMATIC);
    when(mock0.isInfectious()).thenReturn(true);
    when(mock0.reportingCompliance()).thenReturn(1d);
    when(mock0.id()).thenReturn(infector);
    when(mock0.alertStatus()).thenReturn(NONE);

    Map<Integer, Case> population = new HashMap<>();
    population.put(0, mock0);

    ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

    VirusEvent event =
        ImmutableVirusEvent.builder()
            .time(1)
            .id(0)
            .oldStatus(PRESYMPTOMATIC)
            .nextStatus(SYMPTOMATIC)
            .build();

    ProcessedEventResult processedEventResult = eventProcessor.processEvent(event);

    Assert.assertEquals(1, processedEventResult.newVirusEvents().size());
    Assert.assertEquals(1, processedEventResult.newAlertEvents().size());
    Assert.assertEquals(0, processedEventResult.newContactEvents().size());
    Assert.assertEquals(0, processedEventResult.newInfectionEvents().size());
    Assert.assertEquals(1, processedEventResult.newCompletedVirusEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedInfectionEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedAlertEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedContactEvents().size());

    VirusEvent evnt = processedEventResult.newVirusEvents().get(0);

    Assert.assertEquals(
        event.time() + diseaseProperties.timeSymptomsOnset().mean() * properties.timeStepsPerDay(),
        evnt.time());
    Assert.assertEquals(0, evnt.id());
    Assert.assertEquals(SYMPTOMATIC, evnt.oldStatus());
    Assert.assertTrue(SYMPTOMATIC.getValidTransitions().contains(evnt.nextStatus()));
  }

  @Test
  public void runVirusEventsNonCompliant() {

    int infector = 0;

    Case mock0 = mock(Case.class);
    when(mock0.virusStatus()).thenReturn(PRESYMPTOMATIC);
    when(mock0.isInfectious()).thenReturn(true);
    when(mock0.reportingCompliance()).thenReturn(0d);
    when(mock0.id()).thenReturn(infector);
    when(mock0.alertStatus()).thenReturn(NONE);

    Map<Integer, Case> population = new HashMap<>();
    population.put(0, mock0);

    ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

    VirusEvent event =
        ImmutableVirusEvent.builder()
            .time(1)
            .id(0)
            .oldStatus(PRESYMPTOMATIC)
            .nextStatus(SYMPTOMATIC)
            .build();

    ProcessedEventResult processedEventResult = eventProcessor.processEvent(event);

    Assert.assertEquals(1, processedEventResult.newVirusEvents().size());
    Assert.assertEquals(0, processedEventResult.newAlertEvents().size());
    Assert.assertEquals(0, processedEventResult.newContactEvents().size());
    Assert.assertEquals(0, processedEventResult.newInfectionEvents().size());
    Assert.assertEquals(1, processedEventResult.newCompletedVirusEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedInfectionEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedAlertEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedContactEvents().size());

    VirusEvent evnt = processedEventResult.newVirusEvents().get(0);

    Assert.assertEquals(
        event.time() + diseaseProperties.timeLatent().mean() * properties.timeStepsPerDay(),
        evnt.time());
    Assert.assertEquals(0, evnt.id());
    Assert.assertEquals(SYMPTOMATIC, evnt.oldStatus());
    Assert.assertTrue(SYMPTOMATIC.getValidTransitions().contains(evnt.nextStatus()));
  }
}
