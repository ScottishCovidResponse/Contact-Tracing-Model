package uk.co.ramp.policy.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.Population;
import uk.co.ramp.event.CompletionEventListGroup;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.ImmutableContactEvent;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.VirusStatus;

public class AlertContactTracerTest {
  private TracingPolicy tracingPolicy;
  private CompletionEventListGroup eventList;
  private Population population;

  private final ContactEvent event1 =
      ImmutableContactEvent.builder().from(1).to(2).label("label").time(0).weight(1).build();

  private final ContactEvent event2 =
      ImmutableContactEvent.builder().from(1).to(3).label("label").time(0).weight(1).build();

  private final ContactEvent event3 =
      ImmutableContactEvent.builder().from(2).to(1).label("label").time(1).weight(1).build();

  private final ContactEvent event4 =
      ImmutableContactEvent.builder().from(3).to(4).label("label").time(2).weight(1).build();

  private final ContactEvent event5 =
      ImmutableContactEvent.builder().from(4).to(5).label("label").time(1).weight(1).build();

  @Before
  public void setUp() {
    this.tracingPolicy = mock(TracingPolicy.class);
    this.eventList = mock(CompletionEventListGroup.class);
    this.population = mock(Population.class);
  }

  @Test
  public void testPrimaryLevelTrace() {
    when(tracingPolicy.noOfTracingLevels()).thenReturn(1);
    when(eventList.getCompletedContactEventsInPeriod(eq(0), eq(2), eq(1)))
        .thenReturn(List.of(event1, event2, event3));
    when(population.getAlertStatus(anyInt())).thenReturn(AlertStatus.NONE);
    when(population.getVirusStatus(anyInt())).thenReturn(VirusStatus.EXPOSED);
    var contactTracer = new AlertContactTracer(tracingPolicy, eventList, population);

    assertThat(contactTracer.traceRecentContacts(0, 2, 1)).containsExactlyInAnyOrder(2, 3);
  }

  @Test
  public void testSecondaryLevelTrace() {
    when(tracingPolicy.noOfTracingLevels()).thenReturn(2);
    when(eventList.getCompletedContactEventsInPeriod(eq(0), eq(2), eq(1)))
        .thenReturn(List.of(event1, event2, event3));
    when(eventList.getCompletedContactEventsInPeriod(eq(0), eq(2), eq(2)))
        .thenReturn(List.of(event1, event3));
    when(eventList.getCompletedContactEventsInPeriod(eq(0), eq(2), eq(3)))
        .thenReturn(List.of(event2, event4));
    when(population.getAlertStatus(anyInt())).thenReturn(AlertStatus.NONE);
    when(population.getVirusStatus(anyInt())).thenReturn(VirusStatus.EXPOSED);
    var contactTracer = new AlertContactTracer(tracingPolicy, eventList, population);

    assertThat(contactTracer.traceRecentContacts(0, 2, 1)).containsExactlyInAnyOrder(2, 3, 4);
  }

  @Test
  public void testTertiaryLevelTrace() {
    when(tracingPolicy.noOfTracingLevels()).thenReturn(3);
    when(eventList.getCompletedContactEventsInPeriod(eq(0), eq(2), eq(1)))
        .thenReturn(List.of(event1, event2, event3));
    when(eventList.getCompletedContactEventsInPeriod(eq(0), eq(2), eq(2)))
        .thenReturn(List.of(event1, event3));
    when(eventList.getCompletedContactEventsInPeriod(eq(0), eq(2), eq(3)))
        .thenReturn(List.of(event2, event4));
    when(eventList.getCompletedContactEventsInPeriod(eq(0), eq(2), eq(4)))
        .thenReturn(List.of(event5));
    when(eventList.getCompletedContactEventsInPeriod(eq(0), eq(2), eq(5)))
        .thenReturn(List.of(event5));
    when(population.getAlertStatus(anyInt())).thenReturn(AlertStatus.NONE);
    when(population.getVirusStatus(anyInt())).thenReturn(VirusStatus.EXPOSED);
    var contactTracer = new AlertContactTracer(tracingPolicy, eventList, population);

    assertThat(contactTracer.traceRecentContacts(0, 2, 1)).containsExactlyInAnyOrder(2, 3, 4, 5);
  }
}
