package uk.co.ramp.policy.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.distribution.BoundedDistribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.ImmutableAlertEvent;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.VirusStatus;

public class AlertCheckerTest {
  private AlertContactTracer contactTracer;
  private TracingPolicy tracingPolicy;
  private DistributionSampler distributionSampler;
  private BoundedDistribution tracingDelay1Day;
  private BoundedDistribution tracingDelay2Days;
  private BoundedDistribution noSkipTracingProbability;
  private BoundedDistribution allSkipTracingProbability;
  private BoundedDistribution thresholdSkipTracingProbability;
  private ImmutableTracingPolicyItem tracingPolicyItem;

  @Before
  public void setUp() {
    contactTracer = mock(AlertContactTracer.class);
    tracingPolicy = mock(TracingPolicy.class);
    distributionSampler = mock(DistributionSampler.class);
    tracingDelay1Day = mock(BoundedDistribution.class);
    tracingDelay2Days = mock(BoundedDistribution.class);
    noSkipTracingProbability = mock(BoundedDistribution.class);
    allSkipTracingProbability = mock(BoundedDistribution.class);
    thresholdSkipTracingProbability = mock(BoundedDistribution.class);

    when(contactTracer.traceRecentContacts(eq(7), eq(20), eq(1))).thenReturn(Set.of(2, 3));
    when(tracingDelay1Day.getDistributionValue()).thenReturn(1);
    when(tracingDelay2Days.getDistributionValue()).thenReturn(2);
    when(noSkipTracingProbability.getDistributionValue()).thenReturn(0);
    when(allSkipTracingProbability.getDistributionValue()).thenReturn(100);
    when(thresholdSkipTracingProbability.getDistributionValue()).thenReturn(50);
    when(tracingPolicy.probabilitySkippingTraceLinkThreshold())
        .thenReturn(thresholdSkipTracingProbability);
  }

  @Test
  public void testFindRecentContacts_AlertTriggeredWhenSymptomatic() {
    tracingPolicyItem =
        ImmutableTracingPolicyItem.builder()
            .recentContactsLookBackTime(14)
            .reporterAlertStatus(AlertStatus.NONE)
            .reporterVirusStatus(VirusStatus.SYMPTOMATIC)
            .timeDelayPerTraceLink(tracingDelay1Day)
            .probabilitySkippingTraceLink(noSkipTracingProbability)
            .build();
    when(tracingPolicy.policies()).thenReturn(List.of(tracingPolicyItem));

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
    tracingPolicyItem =
        ImmutableTracingPolicyItem.builder()
            .recentContactsLookBackTime(14)
            .reporterAlertStatus(AlertStatus.TESTED_POSITIVE)
            .reporterVirusStatus(VirusStatus.SYMPTOMATIC)
            .timeDelayPerTraceLink(tracingDelay1Day)
            .probabilitySkippingTraceLink(noSkipTracingProbability)
            .build();
    when(tracingPolicy.policies()).thenReturn(List.of(tracingPolicyItem));

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
    tracingPolicyItem =
        ImmutableTracingPolicyItem.builder()
            .recentContactsLookBackTime(14)
            .reporterAlertStatus(AlertStatus.TESTED_POSITIVE)
            .reporterVirusStatus(VirusStatus.SYMPTOMATIC)
            .timeDelayPerTraceLink(tracingDelay1Day)
            .probabilitySkippingTraceLink(noSkipTracingProbability)
            .build();
    when(tracingPolicy.policies()).thenReturn(List.of(tracingPolicyItem));

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
    tracingPolicyItem =
        ImmutableTracingPolicyItem.builder()
            .recentContactsLookBackTime(14)
            .reporterAlertStatus(AlertStatus.NONE)
            .reporterVirusStatus(VirusStatus.SYMPTOMATIC)
            .timeDelayPerTraceLink(tracingDelay2Days)
            .probabilitySkippingTraceLink(noSkipTracingProbability)
            .build();
    when(tracingPolicy.policies()).thenReturn(List.of(tracingPolicyItem));

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
    tracingPolicyItem =
        ImmutableTracingPolicyItem.builder()
            .recentContactsLookBackTime(14)
            .reporterAlertStatus(AlertStatus.NONE)
            .reporterVirusStatus(VirusStatus.SYMPTOMATIC)
            .timeDelayPerTraceLink(tracingDelay1Day)
            .probabilitySkippingTraceLink(allSkipTracingProbability)
            .build();
    when(tracingPolicy.policies()).thenReturn(List.of(tracingPolicyItem));

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
