package uk.co.ramp.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.ImmutableContactEvent;

public class EventListTest {

  private FormattedEventFactory formattedEventFactory;
  private final ImmutableContactEvent event =
      ImmutableContactEvent.builder().time(1).from(0).to(1).label("school").weight(0.5).build();

  @Before
  public void setUp() throws Exception {
    this.formattedEventFactory = mock(FormattedEventFactory.class);
  }

  @Test
  public void testGetEventsInPeriod() {
    var eventList = new EventList<ContactEvent>();
    eventList.addEvent(event.withTime(2).withFrom(0).withTo(1));
    eventList.addEvent(event.withTime(3).withFrom(0).withTo(1));
    eventList.addEvent(event.withTime(3).withFrom(1).withTo(2));
    eventList.addEvent(event.withTime(4).withFrom(1).withTo(3));
    eventList.addEvent(event.withTime(4).withFrom(2).withTo(3));
    eventList.addEvent(event.withTime(4).withFrom(2).withTo(6));
    eventList.addEvent(event.withTime(4).withFrom(3).withTo(6));
    eventList.addEvent(event.withTime(5).withFrom(1).withTo(4));
    eventList.addEvent(event.withTime(6).withFrom(1).withTo(5));
    eventList.addEvent(event.withTime(6).withFrom(1).withTo(5));

    assertThat(eventList.getEventsInPeriod(3, 5, e -> e.from() == 1 || e.to() == 1))
        .containsExactly(
            event.withTime(3).withFrom(0).withTo(1),
            event.withTime(3).withFrom(1).withTo(2),
            event.withTime(4).withFrom(1).withTo(3),
            event.withTime(5).withFrom(1).withTo(4));
  }
}
