package uk.co.ramp.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.assertj.core.data.Offset;
import org.junit.Test;
import uk.co.ramp.TestUtils;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.ImmutableContactEvent;
import uk.co.ramp.io.types.ImmutableStandardProperties;
import uk.co.ramp.io.types.StandardProperties;

public class ContactReaderTest {

  private final Random random = TestUtils.getRandom();

  private final String csv =
      ""
          + "\"time\",\"from\",\"to\",\"weight\",\"label\"\n"
          + "0,8,9,6.7,\"label\"\n"
          + "0,7,8,8.2,\"label\"\n";

  private final StandardProperties defaultProperties =
      ImmutableStandardProperties.builder()
          .populationSize(10000)
          .timeLimit(100)
          .initialExposures(1000)
          .seed(0)
          .steadyState(true)
          .timeStepsPerDay(1)
          .timeStepSpread(1)
          .build();

  private final ContactEvent record1 =
      ImmutableContactEvent.builder().time(0).from(8).to(9).weight(6.7).label("label").build();

  private final ContactEvent record2 =
      ImmutableContactEvent.builder().time(0).from(7).to(8).weight(8.2).label("label").build();

  @Test
  public void testRead() throws IOException {
    StringReader stringReader = new StringReader(csv);
    List<ContactEvent> dailyContactRecords =
        new ContactReader(defaultProperties, TestUtils.dataGenerator()).readEvents(stringReader);

    assertThat(dailyContactRecords).containsExactly(record1, record2);
  }

  @Test
  public void testReadSpread() throws IOException {

    StandardProperties properties =
        ImmutableStandardProperties.builder()
            .from(defaultProperties)
            .timeStepsPerDay(4)
            .timeStepSpread(0.25, 0.25, 0.25, 0.25)
            .build();

    StringReader stringReader = new StringReader(csv);
    List<ContactEvent> dailyContactRecords =
        new ContactReader(properties, TestUtils.dataGenerator()).readEvents(stringReader);

    assertThat(dailyContactRecords).size().isEqualTo(2);

    var spreadRecord1 =
        dailyContactRecords.stream().filter(i -> i.from() == 8).findFirst().orElseThrow();
    var spreadRecord2 =
        dailyContactRecords.stream().filter(i -> i.from() == 7).findFirst().orElseThrow();

    assertThat(spreadRecord1).isEqualToIgnoringGivenFields(record1, "time");
    assertThat(spreadRecord2).isEqualToIgnoringGivenFields(record2, "time");
  }

  @Test
  public void testPersonLimit() throws IOException {
    String csvOverPersonLimit =
        ""
            + "\"time\",\"from\",\"to\",\"weight\",\"label\"\n"
            + "1,10001,10002,6.7,label\n"
            + "2,10000,10001,8.2, label\n";
    StringReader stringReader = new StringReader(csvOverPersonLimit);

    List<ContactEvent> dailyContactRecords =
        new ContactReader(defaultProperties, TestUtils.dataGenerator()).readEvents(stringReader);

    assertThat(dailyContactRecords).isEmpty();
  }

  @Test
  public void testResampleContacts() {

    int days = 5;

    List<ImmutableContactEvent> contactEvents = randomEvents(100, days);

    StandardProperties properties =
        ImmutableStandardProperties.builder()
            .from(defaultProperties)
            .timeStepsPerDay(4)
            .timeStepSpread(0.25, 0.25, 0.25, 0.25)
            .build();

    var minTime =
        contactEvents.stream().map(ContactEvent::time).min(Comparator.naturalOrder()).orElseThrow();

    Map<Integer, Long> counted =
        contactEvents.stream()
            .map(ContactEvent::time)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    ContactReader reader = new ContactReader(properties, TestUtils.dataGenerator());

    List<ImmutableContactEvent> output = reader.resampleContacts(contactEvents);
    var outMaxTime =
        output.stream().map(ContactEvent::time).max(Comparator.naturalOrder()).orElseThrow();
    var outMinTime =
        output.stream().map(ContactEvent::time).min(Comparator.naturalOrder()).orElseThrow();

    var day0 = output.stream().map(ContactEvent::time).filter(between(0, 4)).count();
    var day1 = output.stream().map(ContactEvent::time).filter(between(4, 8)).count();
    var day2 = output.stream().map(ContactEvent::time).filter(between(8, 12)).count();
    var day3 = output.stream().map(ContactEvent::time).filter(between(12, 16)).count();
    var day4 = output.stream().map(ContactEvent::time).filter(between(16, 20)).count();

    assertThat(outMinTime).isEqualTo(minTime);
    assertThat(outMaxTime).isEqualTo(days * properties.timeStepsPerDay() - 1);

    assertThat(day0).isEqualTo(counted.get(0));
    assertThat(day1).isEqualTo(counted.get(1));
    assertThat(day2).isEqualTo(counted.get(2));
    assertThat(day3).isEqualTo(counted.get(3));
    assertThat(day4).isEqualTo(counted.get(4));
  }

  @Test
  public void testResampleContactsGrouping() {

    List<ImmutableContactEvent> contactEvents = randomEvents(1000, 1);

    StandardProperties properties =
        ImmutableStandardProperties.builder()
            .from(defaultProperties)
            .timeStepsPerDay(4)
            .timeStepSpread(0.1, 0.4, 0.3, 0.2)
            .build();
    ;

    ContactReader reader = new ContactReader(properties, TestUtils.dataGenerator());

    List<ImmutableContactEvent> output = reader.resampleContacts(contactEvents);

    var period0 = output.stream().map(ContactEvent::time).filter(between(0, 1)).count();
    var period1 = output.stream().map(ContactEvent::time).filter(between(1, 2)).count();
    var period2 = output.stream().map(ContactEvent::time).filter(between(2, 3)).count();
    var period3 = output.stream().map(ContactEvent::time).filter(between(3, 4)).count();

    Offset<Long> b = Offset.offset((long) Math.sqrt(1000L));
    assertThat(period0).isCloseTo((long) (properties.timeStepSpread()[0] * 1000), b);
    assertThat(period1).isCloseTo((long) (properties.timeStepSpread()[1] * 1000), b);
    assertThat(period2).isCloseTo((long) (properties.timeStepSpread()[2] * 1000), b);
    assertThat(period3).isCloseTo((long) (properties.timeStepSpread()[3] * 1000), b);
  }

  private Predicate<Integer> between(int min, int max) {
    return n -> n < max && n >= min;
  }

  private List<ImmutableContactEvent> randomEvents(int count, int max) {
    List<ImmutableContactEvent> contactEvents = new ArrayList<>();

    for (int i = 0; i < count; i++) {
      contactEvents.add(
          ImmutableContactEvent.builder()
              .time(random.nextInt(max))
              .to(random.nextInt(max))
              .from(random.nextInt(max))
              .weight(random.nextDouble())
              .label("")
              .build());
    }
    return contactEvents;
  }
}
