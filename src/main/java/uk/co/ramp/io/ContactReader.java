package uk.co.ramp.io;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.ImmutableContactEvent;
import uk.co.ramp.io.csv.CsvReader;
import uk.co.ramp.io.types.StandardProperties;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactReader {

    private final StandardProperties properties;

    @Autowired
    public ContactReader(StandardProperties standardProperties) {
        this.properties = standardProperties;
    }

    public List<ContactEvent> readEvents(Reader reader) throws IOException {

        List<ImmutableContactEvent> contactEvents = new CsvReader().read(reader, ImmutableContactEvent.class);

        return contactEvents.stream()
                .filter(event -> event.from() < properties.populationSize())
                .filter(event -> event.to() < properties.populationSize())
                .filter(event -> event.time() <= properties.timeLimit())
                .collect(Collectors.toUnmodifiableList());
    }
}
