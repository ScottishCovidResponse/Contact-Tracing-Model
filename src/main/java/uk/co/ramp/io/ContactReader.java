package uk.co.ramp.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.ImmutableContactEvent;
import uk.co.ramp.io.csv.CsvReader;
import uk.co.ramp.io.types.StandardProperties;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContactReader {
    private static final Logger LOGGER = LogManager.getLogger(ContactReader.class);

    public Map<Integer, List<ContactEvent>> readEvents(Reader reader, StandardProperties properties) throws IOException {

        List<ImmutableContactEvent> contactEvents = new CsvReader().read(reader, ImmutableContactEvent.class);

        Map<Integer, List<ContactEvent>> dailyRecord = new HashMap<>();
        for (ContactEvent event : contactEvents) {
            // This saves time/memory when running a small simulation from a larger contact set.
            if (event.from() >= properties.populationSize() ||
                    event.to() >= properties.populationSize() ||
                    event.time() > properties.timeLimit()) continue;

            dailyRecord.putIfAbsent(event.time(), new ArrayList<>());
            dailyRecord.get(event.time()).add(event);

        }

        return dailyRecord;
    }

}
