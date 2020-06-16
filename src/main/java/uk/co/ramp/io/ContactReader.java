package uk.co.ramp.io;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.contact.ImmutableContactRecord;
import uk.co.ramp.event.processor.ContactEventProcessor;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.ImmutableContactEvent;
import uk.co.ramp.io.csv.CsvReader;
import uk.co.ramp.io.types.StandardProperties;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContactReader {

    private final StandardProperties properties;
    private final ContactEventProcessor contactEventProcessor;

    @Autowired
    public ContactReader(StandardProperties standardProperties, ContactEventProcessor contactEventProcessor) {
        this.properties = standardProperties;
        this.contactEventProcessor = contactEventProcessor;
    }

    Map<Integer, List<ContactRecord>> readRecords(Reader reader) throws IOException {

        List<ImmutableContactRecord> contactRecords = new CsvReader().read(reader, ImmutableContactRecord.class);

        return contactRecords.stream()
                .filter(event -> event.from() < properties.populationSize())
                .filter(event -> event.to() < properties.populationSize())
                .filter(event -> event.time() <= properties.timeLimit())
                .collect(Collectors.groupingBy(ContactRecord::time));
    }

    public Map<Integer, List<ContactEvent>> readEvents(Reader reader) throws IOException {
        Map<Integer, List<ContactRecord>> contactRecords = readRecords(reader);
        return contactRecords.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(record -> ImmutableContactEvent.builder()
                                .time(record.time())
                                .label(record.label())
                                .weight(record.weight())
                                .to(record.to())
                                .from(record.from())
                                .eventProcessor(contactEventProcessor)
                                .build())
                        .collect(Collectors.toList())));
    }
}
