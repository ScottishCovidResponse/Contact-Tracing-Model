package uk.co.ramp.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.junit.Test;
import uk.co.ramp.TestUtils;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.ImmutableContactEvent;
import uk.co.ramp.io.ContactReader;
import uk.co.ramp.io.types.ImmutableStandardProperties;
import uk.co.ramp.io.types.StandardProperties;

public class ContactReaderTest {

  private final String csv =
      ""
          + "\"time\",\"from\",\"to\",\"weight\",\"label\"\n"
          + "0,8,9,6.7,\"label\"\n"
          + "0,7,8,8.2,\"label\"\n";

  private final StandardProperties properties =
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
        new ContactReader(properties, TestUtils.dataGenerator()).readEvents(stringReader);

    assertThat(dailyContactRecords).containsExactly(record1, record2);
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
        new ContactReader(properties, TestUtils.dataGenerator()).readEvents(stringReader);

    assertThat(dailyContactRecords).isEmpty();
  }
}
