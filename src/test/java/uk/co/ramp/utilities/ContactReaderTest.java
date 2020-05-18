package uk.co.ramp.utilities;

import org.junit.Test;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.contact.ImmutableContactRecord;
import uk.co.ramp.io.ImmutableStandardProperties;
import uk.co.ramp.io.StandardProperties;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ContactReaderTest {
    private final String csv = "" +
            "\"time\",\"from\",\"to\",\"weight\"\n" +
            "1,9,10,6.7\n" +
            "1,8,9,8.2\n";

    private final StandardProperties properties = ImmutableStandardProperties.builder()
            .populationSize(10000)
            .timeLimit(100)
            .infected(1000)
            .seed(0)
            .steadyState(true)
            .contactsFile("input/contacts.csv")
            .build();

    private final ContactRecord record1 = ImmutableContactRecord.builder()
            .time(0)
            .from(8)
            .to(9)
            .weight(6.7)
            .build();

    private final ContactRecord record2 = ImmutableContactRecord.builder()
            .time(0)
            .from(7)
            .to(8)
            .weight(8.2)
            .build();

    @Test
    public void testRead() throws IOException {
        StringReader stringReader = new StringReader(csv);
        Map<Integer, List<ContactRecord>> dailyContactRecords = new ContactReader().read(stringReader, properties);

        assertThat(dailyContactRecords).containsOnlyKeys(0);
        assertThat(dailyContactRecords.get(0)).containsExactly(record1, record2);
    }

    @Test
    public void testDayLimit() throws IOException {
        String csvOverDayLimit = "" +
                "\"time\",\"from\",\"to\",\"weight\"\n" +
                "1,9,10,6.7\n" +
                "102,8,9,8.2\n";
        StringReader stringReader = new StringReader(csvOverDayLimit);

        Map<Integer, List<ContactRecord>> dailyContactRecords = new ContactReader().read(stringReader, properties);

        assertThat(dailyContactRecords).containsOnlyKeys(0);
    }

    @Test
    public void testPersonLimit() throws IOException {
        String csvOverPersonLimit = "" +
                "\"time\",\"from\",\"to\",\"weight\"\n" +
                "1,10001,10002,6.7\n" +
                "2,10000,10001,8.2\n";
        StringReader stringReader = new StringReader(csvOverPersonLimit);

        Map<Integer, List<ContactRecord>> dailyContactRecords = new ContactReader().read(stringReader, properties);

        assertThat(dailyContactRecords).isEmpty();
    }
}