package uk.co.ramp.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.co.ramp.people.AlertStatus.*;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.co.ramp.LogSpy;
import uk.co.ramp.Population;
import uk.co.ramp.TestUtils;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.ImmutableAlertEvent;
import uk.co.ramp.event.types.ProcessedEventResult;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.PopulationProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.statistics.StatisticsRecorder;
import uk.co.ramp.statistics.StatisticsRecorderImpl;

public class AlertEventProcessorTest {
  @Rule public LogSpy logSpy = new LogSpy();

  private DiseaseProperties diseaseProperties;
  private Population population;
  private StandardProperties properties;
  private DistributionSampler distributionSampler;
  private AlertEventProcessor eventProcessor;
  private StatisticsRecorder statisticsRecorder;
  private PopulationProperties populationProperties;

  private static final double DELTA = 1e-6;;

  @Before
  public void setUp() throws Exception {
    properties = mock(StandardProperties.class);
    when(properties.timeStepsPerDay()).thenReturn(1);
    diseaseProperties = TestUtils.diseaseProperties();
    population = mock(Population.class);
    distributionSampler = mock(DistributionSampler.class);
    statisticsRecorder = mock(StatisticsRecorderImpl.class);
    populationProperties = mock(PopulationProperties.class);
    when(distributionSampler.getDistributionValue(any())).thenReturn(1);
    when(statisticsRecorder.getFalsePositives()).thenReturn(0);
    when(statisticsRecorder.getFalseNegatives()).thenReturn(0);
  }

  @Test
  public void timeInStatus() {
    eventProcessor =
        new AlertEventProcessor(
            population,
            properties,
            diseaseProperties,
            distributionSampler,
            statisticsRecorder,
            populationProperties);

    AlertEvent alertEvent = mock(AlertEvent.class);

    int time = eventProcessor.timeInStatus(NONE, alertEvent);
    Assert.assertEquals(0, time);

    time = eventProcessor.timeInStatus(ALERTED, alertEvent);
    Assert.assertEquals(1, time);

    time = eventProcessor.timeInStatus(REQUESTED_TEST, alertEvent);
    Assert.assertEquals(
        diseaseProperties.timeTestAdministered().mean() * properties.timeStepsPerDay(),
        time,
        DELTA);

    time = eventProcessor.timeInStatus(AWAITING_RESULT, alertEvent);
    Assert.assertEquals(
        diseaseProperties.timeTestResult().mean() * properties.timeStepsPerDay(), time, DELTA);

    time = eventProcessor.timeInStatus(TESTED_NEGATIVE, alertEvent);
    Assert.assertEquals(1, time);

    time = eventProcessor.timeInStatus(TESTED_POSITIVE, alertEvent);
    Assert.assertEquals(1, time);

  }

  @Test
  public void runAlertEvents() {
    when(population.getVirusStatus(eq(0))).thenReturn(SUSCEPTIBLE);
    when(population.isInfectious(eq(0))).thenReturn(true);
    when(population.getAlertStatus(eq(0))).thenReturn(NONE);

    eventProcessor =
        new AlertEventProcessor(
            population,
            properties,
            diseaseProperties,
            distributionSampler,
            statisticsRecorder,
            populationProperties);

    AlertEvent event =
        ImmutableAlertEvent.builder().time(0).id(0).oldStatus(NONE).nextStatus(ALERTED).build();

    ProcessedEventResult eventResult = eventProcessor.processEvent(event);

    System.out.println(eventResult);

    Assert.assertEquals(1, eventResult.newAlertEvents().size());
    Assert.assertEquals(0, eventResult.newContactEvents().size());
    Assert.assertEquals(0, eventResult.newInfectionEvents().size());
    Assert.assertEquals(0, eventResult.newVirusEvents().size());
    Assert.assertEquals(1, eventResult.newCompletedAlertEvents().size());
    Assert.assertEquals(0, eventResult.newCompletedContactEvents().size());
    Assert.assertEquals(0, eventResult.newCompletedInfectionEvents().size());
    Assert.assertEquals(0, eventResult.newCompletedVirusEvents().size());
    AlertEvent evnt = eventResult.newAlertEvents().get(0);

    Assert.assertEquals(properties.timeStepsPerDay(), evnt.time());
    Assert.assertEquals(0, evnt.id());
    Assert.assertEquals(ALERTED, evnt.oldStatus());
    Assert.assertEquals(REQUESTED_TEST, evnt.nextStatus());
  }

  @Test
  public void testClashingAlertStatus() {
    AlertEvent event =
        ImmutableAlertEvent.builder().time(0).id(0).oldStatus(NONE).nextStatus(ALERTED).build();

    when(population.getAlertStatus(eq(0))).thenReturn(REQUESTED_TEST);
    eventProcessor =
        new AlertEventProcessor(
            population,
            properties,
            diseaseProperties,
            distributionSampler,
            statisticsRecorder,
            populationProperties);

    var processedEvents = eventProcessor.processEvent(event);
    assertThat(processedEvents.newCompletedAlertEvents()).isEmpty();
    assertThat(processedEvents.newCompletedContactEvents()).isEmpty();
    assertThat(processedEvents.newCompletedVirusEvents()).isEmpty();
    assertThat(processedEvents.newCompletedInfectionEvents()).isEmpty();
    assertThat(processedEvents.newAlertEvents()).isEmpty();
    assertThat(processedEvents.newContactEvents()).isEmpty();
    assertThat(processedEvents.newInfectionEvents()).isEmpty();
    assertThat(processedEvents.newVirusEvents()).isEmpty();
    verify(population, times(1)).getAlertStatus(0);
    verify(population, never()).setAlertStatus(0, ALERTED);
    verifyNoMoreInteractions(population);
  }

  @Test
  public void determineTestResult() {

    eventProcessor =
        new AlertEventProcessor(
            population,
            properties,
            diseaseProperties,
            distributionSampler,
            statisticsRecorder,
            populationProperties);

    when(distributionSampler.uniformBetweenZeroAndOne()).thenReturn(0.99d);
    assertThat(eventProcessor.determineTestResult(true)).hasValue(TESTED_NEGATIVE);

    when(distributionSampler.uniformBetweenZeroAndOne()).thenReturn(0.90d);
    assertThat(eventProcessor.determineTestResult(true)).hasValue(TESTED_POSITIVE);

    when(distributionSampler.uniformBetweenZeroAndOne()).thenReturn(0.90d);
    assertThat(eventProcessor.determineTestResult(false)).hasValue(TESTED_NEGATIVE);

    when(distributionSampler.uniformBetweenZeroAndOne()).thenReturn(0.99d);
    assertThat(eventProcessor.determineTestResult(false)).hasValue(TESTED_POSITIVE);
  }
}
