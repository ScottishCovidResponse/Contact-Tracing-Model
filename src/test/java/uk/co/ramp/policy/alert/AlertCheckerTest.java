package uk.co.ramp.policy.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.Population;
import uk.co.ramp.event.CompletionEventListGroup;
import uk.co.ramp.event.types.ImmutableAlertEvent;
import uk.co.ramp.event.types.ImmutableContactEvent;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.VirusStatus;

public class AlertCheckerTest {
  private CompletionEventListGroup eventList;
  private AlertPolicy alertPolicy;
  private Population population;

  @Before
  public void setUp() {
    eventList = mock(CompletionEventListGroup.class);
    alertPolicy = mock(AlertPolicy.class);
    population = mock(Population.class);

    when(eventList.getCompletedContactEventsInPeriod(eq(7), eq(20), eq(1)))
        .thenReturn(
            List.of(
                ImmutableContactEvent.builder()
                    .from(1)
                    .to(2)
                    .time(18)
                    .label("school")
                    .weight(0.5)
                    .build(),
                ImmutableContactEvent.builder()
                    .from(3)
                    .to(1)
                    .time(19)
                    .weight(0.5)
                    .label("home")
                    .build()));
    when(alertPolicy.recentContactsLookBackTime()).thenReturn(14);
    when(population.getVirusStatus(eq(1))).thenReturn(VirusStatus.SYMPTOMATIC);
    when(population.getVirusStatus(eq(2))).thenReturn(VirusStatus.SUSCEPTIBLE);
    when(population.getVirusStatus(eq(3))).thenReturn(VirusStatus.SYMPTOMATIC);
    when(population.getAlertStatus(eq(1))).thenReturn(AlertStatus.NONE);
    when(population.getAlertStatus(eq(2))).thenReturn(AlertStatus.NONE);
    when(population.getAlertStatus(eq(3))).thenReturn(AlertStatus.NONE);
  }

  @Test
  public void testFindRecentContacts() {
    var alertChecker = new AlertChecker(alertPolicy, eventList, population);

    var event =
        ImmutableAlertEvent.builder()
            .id(1)
            .time(21)
            .oldStatus(AlertStatus.NONE)
            .nextStatus(AlertStatus.ALERTED)
            .build();
    assertThat(alertChecker.checkForAlert(1, VirusStatus.SYMPTOMATIC, 20))
        .containsExactly(
            event.withNextStatus(AlertStatus.REQUESTED_TEST), event.withId(2), event.withId(3));
  }
}
