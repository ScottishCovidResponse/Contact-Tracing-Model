package uk.co.ramp.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.contact.ImmutableContactRecord;
import uk.co.ramp.event.ContactEvent;
import uk.co.ramp.event.ImmutableContactEvent;
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

    public Map<Integer, List<ContactRecord>> read(Reader reader, StandardProperties runProperties) throws IOException {
        return read(reader, runProperties.populationSize());
    }

    public Map<Integer, List<ContactEvent>> readEvents(Reader fileReader, StandardProperties standardProperties) throws IOException {
        return readEvents(fileReader, standardProperties.populationSize());
    }


    private Map<Integer, List<ContactEvent>> readEvents(Reader reader, int personLimit) throws IOException {
        List<ImmutableContactEvent> contactEvents = new CsvReader().read(reader, ImmutableContactEvent.class);

        Map<Integer, List<ContactEvent>> dailyRecord = new HashMap<>();
        for (ContactEvent event : contactEvents) {
            // This saves time/memory when running a small simulation from a larger contact set.
            if (event.from() >= personLimit || event.to() >= personLimit) continue;

            dailyRecord.putIfAbsent(event.time(), new ArrayList<>());
            dailyRecord.get(event.time()).add(event);

        }

        return dailyRecord;
    }


    private Map<Integer, List<ContactRecord>> read(Reader reader, int personLimit) throws IOException {
        List<ImmutableContactRecord> contactRecords = new CsvReader().read(reader, ImmutableContactRecord.class);
        double maxWeight = 0d;
        Map<Integer, List<ContactRecord>> dailyRecord = new HashMap<>();
        for (ContactRecord record : contactRecords) {

            // we want these as zero indexed.
            int day = record.time();
            int from = record.from();
            int to = record.to();
            double weight = record.weight();
            String label = record.label();

            // This saves time/memory when running a small simulation from a larger contact set.
            if (from >= personLimit || to >= personLimit) continue;

            ContactRecord c = ImmutableContactRecord.builder().time(day).from(from).to(to).weight(weight).label(label).build();
            dailyRecord.putIfAbsent(day, new ArrayList<>());
            dailyRecord.get(day).add(c);

            if (weight > maxWeight) {
                maxWeight = weight;
            }

        }

        LOGGER.info("maxWeight = {}", maxWeight);

        return dailyRecord;
    }


}
