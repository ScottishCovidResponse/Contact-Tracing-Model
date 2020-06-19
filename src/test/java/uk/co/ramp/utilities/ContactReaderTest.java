package uk.co.ramp.utilities;

import org.junit.Test;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.contact.ImmutableContactRecord;
import uk.co.ramp.event.processor.ContactEventProcessor;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.ImmutableContactEvent;
import uk.co.ramp.io.ContactReader;
import uk.co.ramp.io.types.ImmutableStandardProperties;
import uk.co.ramp.io.types.StandardProperties;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ContactReaderTest {
    private final String csv = "" +
            "\"time\",\"from\",\"to\",\"weight\",\"label\"\n" +
            "0,8,9,6.7,\"label\"\n" +
            "0,7,8,8.2,\"label\"\n";

    private final StandardProperties properties = ImmutableStandardProperties.builder()
            .populationSize(10000)
            .timeLimit(100)
            .initialExposures(1000)
            .seed(0)
            .steadyState(true)
            .build();

    private ContactEventProcessor eventProcessor = mock(ContactEventProcessor.class);
    private final ContactEvent record1 = ImmutableContactEvent.builder()
            .time(0)
            .from(8)
            .to(9)
            .weight(6.7)
            .label("label")
            .eventProcessor(eventProcessor)
            .build();

    private final ContactEvent record2 = ImmutableContactEvent.builder()
            .time(0)
            .from(7)
            .to(8)
            .weight(8.2)
            .label("label")
            .eventProcessor(eventProcessor)
            .build();

    @Test
    public void testRead() throws IOException {
        StringReader stringReader = new StringReader(csv);
        Map<Integer, List<ContactEvent>> dailyContactRecords = new ContactReader(properties, eventProcessor).readEvents(stringReader);

        assertThat(dailyContactRecords).containsOnlyKeys(0);
        assertThat(dailyContactRecords.get(0)).containsExactly(record1, record2);
    }


    @Test
    public void testPersonLimit() throws IOException {
        String csvOverPersonLimit = "" +
                "\"time\",\"from\",\"to\",\"weight\",\"label\"\n" +
                "1,10001,10002,6.7,label\n" +
                "2,10000,10001,8.2, label\n";
        StringReader stringReader = new StringReader(csvOverPersonLimit);

        Map<Integer, List<ContactEvent>> dailyContactRecords = new ContactReader(properties, eventProcessor).readEvents(stringReader);

        assertThat(dailyContactRecords).isEmpty();
    }
}