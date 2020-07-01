package uk.co.ramp.policy.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.Distribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.ImmutableAlertEvent;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.VirusStatus;

public class AlertCheckerTest {
  private AlertContactTracer contactTracer;
  private TracingPolicy tracingPolicy;
  private Population population;
  private DistributionSampler distributionSampler;
  private Distribution tracingDelay1Day;
  private Distribution tracingDelay2Days;

  @Before
  public void setUp() {
    contactTracer = mock(AlertContactTracer.class);
    tracingPolicy = mock(TracingPolicy.class);
    population = mock(Population.class);
    distributionSampler = mock(DistributionSampler.class);
    tracingDelay1Day = mock(Distribution.class);
    tracingDelay2Days = mock(Distribution.class);

    when(contactTracer.traceRecentContacts(eq(7), eq(20), eq(1))).thenReturn(Set.of(2, 3));
    when(tracingPolicy.recentContactsLookBackTime()).thenReturn(14);
    when(population.getVirusStatus(eq(2))).thenReturn(VirusStatus.SUSCEPTIBLE);
    when(population.getVirusStatus(eq(3))).thenReturn(VirusStatus.SYMPTOMATIC);
    when(population.getAlertStatus(eq(2))).thenReturn(AlertStatus.NONE);
    when(population.getAlertStatus(eq(3))).thenReturn(AlertStatus.NONE);
    when(distributionSampler.getDistributionValue(eq(tracingDelay1Day))).thenReturn(1);
    when(distributionSampler.getDistributionValue(eq(tracingDelay2Days))).thenReturn(2);
  }

  @Test
  public void testFindRecentContacts_AlertTriggeredWhenSymptomatic() {
    when(tracingPolicy.reporterAlertStatus()).thenReturn(AlertStatus.NONE);
    when(tracingPolicy.reporterVirusStatus()).thenReturn(VirusStatus.SYMPTOMATIC);
    when(population.getVirusStatus(eq(1))).thenReturn(VirusStatus.SYMPTOMATIC);
    when(population.getAlertStatus(eq(1))).thenReturn(AlertStatus.NONE);
    when(tracingPolicy.delayPerTraceLink()).thenReturn(tracingDelay1Day);

    var alertChecker =
        new AlertChecker(tracingPolicy, contactTracer, population, distributionSampler);

    var event =
        ImmutableAlertEvent.builder()
            .id(1)
            .time(21)
            .oldStatus(AlertStatus.NONE)
            .nextStatus(AlertStatus.ALERTED)
            .build();
    assertThat(alertChecker.checkForAlert(1, VirusStatus.SYMPTOMATIC, 20))
        .containsExactlyInAnyOrder(
            event.withNextStatus(AlertStatus.REQUESTED_TEST), event.withId(2), event.withId(3));
  }

  @Test
  public void testFindRecentContacts_OnlyAlertAfterPositiveTest_RequestTest() {
    when(tracingPolicy.reporterAlertStatus()).thenReturn(AlertStatus.TESTED_POSITIVE);
    when(tracingPolicy.reporterVirusStatus()).thenReturn(VirusStatus.SYMPTOMATIC);
    when(population.getVirusStatus(eq(1))).thenReturn(VirusStatus.SYMPTOMATIC);
    when(population.getAlertStatus(eq(1))).thenReturn(AlertStatus.NONE);
    when(tracingPolicy.delayPerTraceLink()).thenReturn(tracingDelay1Day);

    var alertChecker =
        new AlertChecker(tracingPolicy, contactTracer, population, distributionSampler);

    var event =
        ImmutableAlertEvent.builder()
            .id(1)
            .time(21)
            .oldStatus(AlertStatus.NONE)
            .nextStatus(AlertStatus.REQUESTED_TEST)
            .build();
    assertThat(alertChecker.checkForAlert(1, VirusStatus.SYMPTOMATIC, 20))
        .containsExactlyInAnyOrder(event);
  }

  @Test
  public void testFindRecentContacts_OnlyAlertAfterPositiveTest_AlertContacts() {
    when(tracingPolicy.reporterAlertStatus()).thenReturn(AlertStatus.TESTED_POSITIVE);
    when(tracingPolicy.reporterVirusStatus()).thenReturn(VirusStatus.SYMPTOMATIC);
    when(population.getVirusStatus(eq(1))).thenReturn(VirusStatus.SYMPTOMATIC);
    when(population.getAlertStatus(eq(1))).thenReturn(AlertStatus.TESTED_POSITIVE);
    when(tracingPolicy.delayPerTraceLink()).thenReturn(tracingDelay1Day);

    var alertChecker =
        new AlertChecker(tracingPolicy, contactTracer, population, distributionSampler);

    var event =
        ImmutableAlertEvent.builder()
            .id(2)
            .time(21)
            .oldStatus(AlertStatus.NONE)
            .nextStatus(AlertStatus.ALERTED)
            .build();
    assertThat(alertChecker.checkForAlert(1, VirusStatus.SYMPTOMATIC, 20))
        .containsExactlyInAnyOrder(event, event.withId(3));
  }

  @Test
  public void testFindRecentContacts_DelayedTracing() {
    when(tracingPolicy.reporterAlertStatus()).thenReturn(AlertStatus.NONE);
    when(tracingPolicy.reporterVirusStatus()).thenReturn(VirusStatus.SYMPTOMATIC);
    when(population.getVirusStatus(eq(1))).thenReturn(VirusStatus.SYMPTOMATIC);
    when(population.getAlertStatus(eq(1))).thenReturn(AlertStatus.NONE);
    when(tracingPolicy.delayPerTraceLink()).thenReturn(tracingDelay2Days);

    var alertChecker =
        new AlertChecker(tracingPolicy, contactTracer, population, distributionSampler);

    var event =
        ImmutableAlertEvent.builder()
            .id(1)
            .time(21)
            .oldStatus(AlertStatus.NONE)
            .nextStatus(AlertStatus.ALERTED)
            .build();
    assertThat(alertChecker.checkForAlert(1, VirusStatus.SYMPTOMATIC, 20))
        .containsExactlyInAnyOrder(
            event.withNextStatus(AlertStatus.REQUESTED_TEST),
            event.withId(2).withTime(22),
            event.withId(3).withTime(22));
  }
}
