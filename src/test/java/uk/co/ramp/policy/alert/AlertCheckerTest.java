package uk.co.ramp.policy.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.distribution.Distribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.ImmutableAlertEvent;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.policy.alert.TracingPolicy.TracingPolicyItem;

public class AlertCheckerTest {
  private AlertContactTracer contactTracer;
  private TracingPolicy tracingPolicy;
  private DistributionSampler distributionSampler;
  private Distribution tracingDelay1Day;
  private Distribution tracingDelay2Days;
  private Distribution noSkipTracingProbability;
  private Distribution allSkipTracingProbability;
  private Distribution thresholdSkipTracingProbability;
  private TracingPolicyItem tracingPolicyItem;

  @Before
  public void setUp() {
    contactTracer = mock(AlertContactTracer.class);
    tracingPolicy = mock(TracingPolicy.class);
    distributionSampler = mock(DistributionSampler.class);
    tracingDelay1Day = mock(Distribution.class);
    tracingDelay2Days = mock(Distribution.class);
    noSkipTracingProbability = mock(Distribution.class);
    allSkipTracingProbability = mock(Distribution.class);
    thresholdSkipTracingProbability = mock(Distribution.class);
    tracingPolicyItem = mock(TracingPolicyItem.class);

    when(contactTracer.traceRecentContacts(eq(7), eq(20), eq(1))).thenReturn(Set.of(2, 3));
    when(tracingPolicy.policies()).thenReturn(List.of(tracingPolicyItem));
    when(tracingPolicyItem.recentContactsLookBackTime()).thenReturn(14);
    when(distributionSampler.getDistributionValue(eq(tracingDelay1Day))).thenReturn(1);
    when(distributionSampler.getDistributionValue(eq(tracingDelay2Days))).thenReturn(2);
    when(distributionSampler.getDistributionValue(eq(noSkipTracingProbability))).thenReturn(0);
    when(distributionSampler.getDistributionValue(eq(allSkipTracingProbability))).thenReturn(100);
    when(distributionSampler.getDistributionValue(eq(thresholdSkipTracingProbability)))
        .thenReturn(50);
    when(tracingPolicy.probabilitySkippingTraceLinkThreshold())
        .thenReturn(thresholdSkipTracingProbability);
  }

  @Test
  public void testFindRecentContacts_AlertTriggeredWhenSymptomatic() {
    when(tracingPolicyItem.reporterAlertStatus()).thenReturn(AlertStatus.NONE);
    when(tracingPolicyItem.reporterVirusStatus()).thenReturn(VirusStatus.SYMPTOMATIC);
    when(tracingPolicyItem.timeDelayPerTraceLink()).thenReturn(tracingDelay1Day);
    when(tracingPolicyItem.probabilitySkippingTraceLink()).thenReturn(noSkipTracingProbability);

    var alertChecker = new AlertChecker(tracingPolicy, contactTracer, distributionSampler);

    var event =
        ImmutableAlertEvent.builder()
            .id(1)
            .time(21)
            .oldStatus(AlertStatus.NONE)
            .nextStatus(AlertStatus.ALERTED)
            .build();
    assertThat(alertChecker.checkForAlert(1, AlertStatus.NONE, VirusStatus.SYMPTOMATIC, 20))
        .containsExactlyInAnyOrder(
            event.withNextStatus(AlertStatus.REQUESTED_TEST), event.withId(2), event.withId(3));
  }

  @Test
  public void testFindRecentContacts_OnlyAlertAfterPositiveTest_RequestTest() {
    when(tracingPolicyItem.reporterAlertStatus()).thenReturn(AlertStatus.TESTED_POSITIVE);
    when(tracingPolicyItem.reporterVirusStatus()).thenReturn(VirusStatus.SYMPTOMATIC);
    when(tracingPolicyItem.timeDelayPerTraceLink()).thenReturn(tracingDelay1Day);
    when(tracingPolicyItem.probabilitySkippingTraceLink()).thenReturn(noSkipTracingProbability);

    var alertChecker = new AlertChecker(tracingPolicy, contactTracer, distributionSampler);

    var event =
        ImmutableAlertEvent.builder()
            .id(1)
            .time(21)
            .oldStatus(AlertStatus.NONE)
            .nextStatus(AlertStatus.REQUESTED_TEST)
            .build();
    assertThat(alertChecker.checkForAlert(1, AlertStatus.NONE, VirusStatus.SYMPTOMATIC, 20))
        .containsExactlyInAnyOrder(event);
  }

  @Test
  public void testFindRecentContacts_OnlyAlertAfterPositiveTest_AlertContacts() {
    when(tracingPolicyItem.reporterAlertStatus()).thenReturn(AlertStatus.TESTED_POSITIVE);
    when(tracingPolicyItem.reporterVirusStatus()).thenReturn(VirusStatus.SYMPTOMATIC);
    when(tracingPolicyItem.timeDelayPerTraceLink()).thenReturn(tracingDelay1Day);
    when(tracingPolicyItem.probabilitySkippingTraceLink()).thenReturn(noSkipTracingProbability);

    var alertChecker = new AlertChecker(tracingPolicy, contactTracer, distributionSampler);

    var event =
        ImmutableAlertEvent.builder()
            .id(2)
            .time(21)
            .oldStatus(AlertStatus.NONE)
            .nextStatus(AlertStatus.ALERTED)
            .build();
    assertThat(
            alertChecker.checkForAlert(1, AlertStatus.TESTED_POSITIVE, VirusStatus.SYMPTOMATIC, 20))
        .containsExactlyInAnyOrder(event, event.withId(3));
  }

  @Test
  public void testFindRecentContacts_DelayedTracing() {
    when(tracingPolicyItem.reporterAlertStatus()).thenReturn(AlertStatus.NONE);
    when(tracingPolicyItem.reporterVirusStatus()).thenReturn(VirusStatus.SYMPTOMATIC);
    when(tracingPolicyItem.timeDelayPerTraceLink()).thenReturn(tracingDelay2Days);
    when(tracingPolicyItem.probabilitySkippingTraceLink()).thenReturn(noSkipTracingProbability);

    var alertChecker = new AlertChecker(tracingPolicy, contactTracer, distributionSampler);

    var event =
        ImmutableAlertEvent.builder()
            .id(1)
            .time(21)
            .oldStatus(AlertStatus.NONE)
            .nextStatus(AlertStatus.ALERTED)
            .build();
    assertThat(alertChecker.checkForAlert(1, AlertStatus.NONE, VirusStatus.SYMPTOMATIC, 20))
        .containsExactlyInAnyOrder(
            event.withNextStatus(AlertStatus.REQUESTED_TEST),
            event.withId(2).withTime(22),
            event.withId(3).withTime(22));
  }

  @Test
  public void testFindRecentContacts_SkipTracing() {
    when(tracingPolicyItem.reporterAlertStatus()).thenReturn(AlertStatus.NONE);
    when(tracingPolicyItem.reporterVirusStatus()).thenReturn(VirusStatus.SYMPTOMATIC);
    when(tracingPolicyItem.timeDelayPerTraceLink()).thenReturn(tracingDelay1Day);
    when(tracingPolicyItem.probabilitySkippingTraceLink()).thenReturn(allSkipTracingProbability);

    var alertChecker = new AlertChecker(tracingPolicy, contactTracer, distributionSampler);

    var event =
        ImmutableAlertEvent.builder()
            .id(1)
            .time(21)
            .oldStatus(AlertStatus.NONE)
            .nextStatus(AlertStatus.REQUESTED_TEST)
            .build();
    assertThat(alertChecker.checkForAlert(1, AlertStatus.NONE, VirusStatus.SYMPTOMATIC, 20))
        .containsExactly(event);
  }
}
