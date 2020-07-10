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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ramp.*;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.ImmutableAlertEvent;
import uk.co.ramp.event.types.ProcessedEventResult;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.StandardProperties;

@RunWith(SpringRunner.class)
@DirtiesContext
@Import({TestUtils.class, AppConfig.class, TestConfig.class})
public class AlertEventProcessorTest {
  @Rule public LogSpy logSpy = new LogSpy();

  private DiseaseProperties diseaseProperties;
  private Population population;
  @Autowired private StandardProperties properties;
  private DistributionSampler distributionSampler;
  private AlertEventProcessor eventProcessor;

  @Before
  public void setUp() throws Exception {
    diseaseProperties = TestUtils.diseaseProperties();
    population = mock(Population.class);
    distributionSampler = mock(DistributionSampler.class);
    when(distributionSampler.getDistributionValue(any())).thenReturn(1);
  }

  @Test
  public void timeInStatus() {
    eventProcessor =
        new AlertEventProcessor(population, properties, diseaseProperties, distributionSampler);

    int time = eventProcessor.timeInStatus(NONE);
    Assert.assertEquals(0, time);

    time = eventProcessor.timeInStatus(ALERTED);
    Assert.assertEquals(1, time);

    time = eventProcessor.timeInStatus(REQUESTED_TEST);
    Assert.assertEquals(diseaseProperties.timeTestAdministered().mean(), time);

    time = eventProcessor.timeInStatus(AWAITING_RESULT);
    Assert.assertEquals(diseaseProperties.timeTestResult().mean(), time);

    time = eventProcessor.timeInStatus(TESTED_NEGATIVE);
    Assert.assertEquals(1, time);

    time = eventProcessor.timeInStatus(TESTED_POSITIVE);
    Assert.assertEquals(1, time);
  }

  @Test
  public void runAlertEvents() {
    when(population.getVirusStatus(eq(0))).thenReturn(SUSCEPTIBLE);
    when(population.isInfectious(eq(0))).thenReturn(true);
    when(population.getAlertStatus(eq(0))).thenReturn(NONE);

    eventProcessor =
        new AlertEventProcessor(population, properties, diseaseProperties, distributionSampler);

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

    Assert.assertEquals(1, evnt.time());
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
        new AlertEventProcessor(population, properties, diseaseProperties, distributionSampler);

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
}
