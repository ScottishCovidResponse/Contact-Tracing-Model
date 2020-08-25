package uk.co.ramp.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.co.ramp.people.AlertStatus.*;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

import org.apache.commons.math3.random.RandomGenerator;
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
  private RandomGenerator rng;

  private static final double DELTA = 1e-6;

  @Before
  public void setUp() throws Exception {
    properties = mock(StandardProperties.class);
    when(properties.timeStepsPerDay()).thenReturn(1);
    when(properties.timeLimitDays()).thenReturn(27);
    when(properties.populationSize()).thenReturn(10000);
    diseaseProperties = TestUtils.diseaseProperties();
    population = mock(Population.class);
    distributionSampler = mock(DistributionSampler.class);
    statisticsRecorder = mock(StatisticsRecorderImpl.class);
    when(statisticsRecorder.getFalsePositives()).thenReturn(0);
    when(statisticsRecorder.getFalseNegatives()).thenReturn(0);
    populationProperties = mock(PopulationProperties.class);
    when(populationProperties.testCapacity()).thenReturn(0.01);
    rng = mock(RandomGenerator.class);
    when(rng.nextDouble()).thenReturn(0.5D);
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
            populationProperties,
            rng);

    AlertEvent alertEvent = mock(AlertEvent.class);

    int time = eventProcessor.timeInStatusAndTestQueue(NONE, alertEvent);
    Assert.assertEquals(0, time);

    time = eventProcessor.timeInStatusAndTestQueue(ALERTED, alertEvent);
    Assert.assertEquals(1, time);

    time = eventProcessor.timeInStatusAndTestQueue(REQUESTED_TEST, alertEvent);

    Assert.assertEquals(
        diseaseProperties.timeTestAdministered().getDistributionValue()
            * properties.timeStepsPerDay(),
        time,
        DELTA);

    time = eventProcessor.timeInStatusAndTestQueue(AWAITING_RESULT, alertEvent);
    Assert.assertEquals(
        diseaseProperties.timeTestResult().getDistributionValue() * properties.timeStepsPerDay(),
        time,
        DELTA);

    time = eventProcessor.timeInStatusAndTestQueue(TESTED_NEGATIVE, alertEvent);
    Assert.assertEquals(1, time);

    time = eventProcessor.timeInStatusAndTestQueue(TESTED_POSITIVE, alertEvent);
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
            populationProperties,
            rng);

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

    Assert.assertEquals(
        diseaseProperties.timeTestAdministered().getDistributionValue()
            * properties.timeStepsPerDay(),
        evnt.time());
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
            populationProperties,
            rng);

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
            populationProperties,
            rng);

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
