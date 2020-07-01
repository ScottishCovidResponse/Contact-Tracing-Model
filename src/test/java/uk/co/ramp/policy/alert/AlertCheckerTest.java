package uk.co.ramp.policy.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.Population;
import uk.co.ramp.event.types.ImmutableAlertEvent;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.VirusStatus;

public class AlertCheckerTest {
  private AlertContactTracer contactTracer;
  private TracingPolicy tracingPolicy;
  private Population population;

  @Before
  public void setUp() {
    contactTracer = mock(AlertContactTracer.class);
    tracingPolicy = mock(TracingPolicy.class);
    population = mock(Population.class);

    when(contactTracer.traceRecentContacts(eq(7), eq(20), eq(1))).thenReturn(Set.of(2, 3));
    when(tracingPolicy.recentContactsLookBackTime()).thenReturn(14);
    when(tracingPolicy.reporterAlertStatus()).thenReturn(AlertStatus.NONE);
    when(tracingPolicy.reporterVirusStatus()).thenReturn(VirusStatus.SYMPTOMATIC);
    when(population.getVirusStatus(eq(1))).thenReturn(VirusStatus.SYMPTOMATIC);
    when(population.getVirusStatus(eq(2))).thenReturn(VirusStatus.SUSCEPTIBLE);
    when(population.getVirusStatus(eq(3))).thenReturn(VirusStatus.SYMPTOMATIC);
    when(population.getAlertStatus(eq(1))).thenReturn(AlertStatus.NONE);
    when(population.getAlertStatus(eq(2))).thenReturn(AlertStatus.NONE);
    when(population.getAlertStatus(eq(3))).thenReturn(AlertStatus.NONE);
  }

  @Test
  public void testFindRecentContacts() {
    var alertChecker = new AlertChecker(tracingPolicy, contactTracer, population);

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
}
