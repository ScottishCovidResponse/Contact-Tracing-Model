package uk.co.ramp.event;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;

import java.util.Random;
import net.bytebuddy.utility.RandomString;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import uk.co.ramp.LogSpy;
import uk.co.ramp.TestUtils;
import uk.co.ramp.event.types.*;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;

public class FormattedEventFactoryTest {

  private final Random random = TestUtils.getRandom();

  private final FormattedEventFactory formattedEventFactory = new FormattedEventFactory();

  @Rule public LogSpy logSpy = new LogSpy();

  @Test
  public void createVirusEvent() {

    int time = random.nextInt(100);
    int id = random.nextInt(100);
    VirusStatus old = VirusStatus.EXPOSED;
    VirusStatus next = VirusStatus.PRESYMPTOMATIC;

    VirusEvent virusEvent =
        ImmutableVirusEvent.builder().time(time).id(id).oldStatus(old).nextStatus(next).build();

    ImmutableFormattedEvent formatted = formattedEventFactory.create(virusEvent);

    Assert.assertEquals(id, formatted.id());
    Assert.assertEquals(time, formatted.time());
    Assert.assertEquals(next.toString(), formatted.newStatus());
    Assert.assertThat(formatted.additionalInfo(), containsString(old.toString()));
  }

  @Test
  public void createInfectionEvent() {

    int time = random.nextInt(100);
    int id = random.nextInt(100);
    int exposer = random.nextInt(100);
    int exposedTime = time - 1;
    VirusStatus old = VirusStatus.SUSCEPTIBLE;
    VirusStatus next = VirusStatus.EXPOSED;

    InfectionEvent infectionEvent =
        ImmutableInfectionEvent.builder()
            .time(time)
            .id(id)
            .oldStatus(old)
            .nextStatus(next)
            .exposedBy(exposer)
            .exposedTime(exposedTime)
            .build();

    ImmutableFormattedEvent formatted = formattedEventFactory.create(infectionEvent);

    Assert.assertEquals(id, formatted.id());
    Assert.assertEquals(time, formatted.time());
    Assert.assertEquals(next.toString(), formatted.newStatus());
    Assert.assertThat(formatted.additionalInfo(), containsString("" + exposer));
    Assert.assertThat(formatted.additionalInfo(), containsString("" + exposedTime));

    InfectionEvent specific =
        ImmutableInfectionEvent.copyOf(infectionEvent).withExposedBy(Case.getInitial());
    formatted = formattedEventFactory.create(specific);
    Assert.assertThat(
        formatted.additionalInfo(), containsString("This case was an initial infection"));

    specific =
        ImmutableInfectionEvent.copyOf(infectionEvent).withExposedBy(Case.getRandomInfection());
    formatted = formattedEventFactory.create(specific);
    Assert.assertThat(
        formatted.additionalInfo(), containsString("This case was a random infection"));
  }

  @Test
  public void createPolicyEvent() {
    // TODO will need to fill out
    int time = random.nextInt(100);
    PolicyEvent policyEvent = ImmutablePolicyEvent.builder().time(time).build();

    ImmutableFormattedEvent formatted = formattedEventFactory.create(policyEvent);

    Assert.assertEquals(0, formatted.id());
    Assert.assertEquals(time, formatted.time());
    Assert.assertEquals("TODO will need to fill out", formatted.additionalInfo());
  }

  @Test
  public void createAlertEvent() {

    int time = random.nextInt(100);
    int id = random.nextInt(100);
    AlertStatus old = AlertStatus.NONE;
    AlertStatus next = AlertStatus.ALERTED;

    AlertEventProcessor eventProcessor = mock(AlertEventProcessor.class);

    AlertEvent alertEvent =
        ImmutableAlertEvent.builder().time(time).id(id).nextStatus(next).oldStatus(old).build();

    ImmutableFormattedEvent formatted = formattedEventFactory.create(alertEvent);

    Assert.assertEquals(id, formatted.id());
    Assert.assertEquals(time, formatted.time());
    Assert.assertEquals(next.toString(), formatted.newStatus());
  }

  @Test
  public void createContactEvent() {
    int time = random.nextInt(100);
    int id = random.nextInt(100);
    int id2 = random.nextInt(100);
    double weight = random.nextDouble() * random.nextInt(100);
    String label = RandomString.make(20);

    ContactEvent contactEvent =
        ImmutableContactEvent.builder()
            .time(time)
            .to(id)
            .from(id2)
            .weight(weight)
            .label(label)
            .build();

    ImmutableFormattedEvent formatted = formattedEventFactory.create(contactEvent);

    System.out.println(formatted);
    Assert.assertEquals(id, formatted.id());
    Assert.assertEquals(time, formatted.time());
    Assert.assertEquals(label, formatted.newStatus());
    Assert.assertThat(formatted.additionalInfo(), containsString("" + weight));
  }

  @Test
  public void createEvent() {

    int time = random.nextInt(100);
    int id = random.nextInt(100);
    int id2 = random.nextInt(100);
    double weight = random.nextDouble() * random.nextInt(100);
    String label = RandomString.make(20);

    AlertStatus oldAlert = AlertStatus.NONE;
    AlertStatus nextAlert = AlertStatus.ALERTED;

    int exposer = random.nextInt(100);
    int exposedTime = time - 1;
    VirusStatus oldVirus = VirusStatus.SUSCEPTIBLE;
    VirusStatus nextVirus = VirusStatus.EXPOSED;

    Event contactEvent =
        ImmutableContactEvent.builder()
            .time(time)
            .to(id)
            .from(id2)
            .weight(weight)
            .label(label)
            .build();
    Event policyEvent = ImmutablePolicyEvent.builder().time(time).build();
    Event alertEvent =
        ImmutableAlertEvent.builder()
            .time(time)
            .id(id)
            .nextStatus(nextAlert)
            .oldStatus(oldAlert)
            .build();
    Event infectionEvent =
        ImmutableInfectionEvent.builder()
            .time(time)
            .id(id)
            .oldStatus(oldVirus)
            .nextStatus(nextVirus)
            .exposedBy(exposer)
            .exposedTime(exposedTime)
            .build();
    Event virusEvent =
        ImmutableVirusEvent.builder()
            .time(time)
            .id(id)
            .oldStatus(oldVirus)
            .nextStatus(nextVirus)
            .build();
    Event notCovered = mock(CommonVirusEvent.class);

    // TODO: not printing these as they're an input... should discuss with group.
    ImmutableFormattedEvent event = formattedEventFactory.create(contactEvent);
    Assert.assertNull(event);

    event = formattedEventFactory.create(alertEvent);
    Assert.assertNotNull(event);
    Assert.assertNotNull(event.eventType());
    Assert.assertEquals("AlertEvent", event.eventType());

    event = formattedEventFactory.create(infectionEvent);
    Assert.assertNotNull(event);
    Assert.assertNotNull(event.eventType());
    Assert.assertEquals("InfectionEvent", event.eventType());

    event = formattedEventFactory.create(virusEvent);
    Assert.assertNotNull(event);
    Assert.assertNotNull(event.eventType());
    Assert.assertEquals("VirusEvent", event.eventType());

    event = formattedEventFactory.create(policyEvent);
    Assert.assertNotNull(event);
    Assert.assertNotNull(event.eventType());
    Assert.assertEquals("PolicyEvent", event.eventType());

    assertThatExceptionOfType(EventException.class)
        .isThrownBy(() -> formattedEventFactory.create(notCovered))
        .withMessageContaining("Unknown Event Type")
        .withMessageContaining(notCovered.getClass().getSimpleName());
  }
}
