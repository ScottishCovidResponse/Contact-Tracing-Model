package uk.co.ramp.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.contact.ImmutableContactRecord;

import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.io.csv.CsvReader;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactReader {
    private static final Logger LOGGER = LogManager.getLogger(ContactReader.class);

    public Map<Integer, List<ContactRecord>> read(Reader reader, StandardProperties runProperties) throws IOException {
        return read(reader, runProperties.populationSize(), runProperties.timeLimit());
    }


    private Map<Integer, List<ContactRecord>> read(Reader reader, int personLimit, int dayLimit) throws IOException {
        List<ImmutableContactRecord> contactRecords = new CsvReader().read(reader, ImmutableContactRecord.class);
        double maxWeight = 0d;
        Map<Integer, List<ContactRecord>> dailyRecord = new HashMap<>();
        for (ContactRecord record : contactRecords) {

            int day = record.time() - 1;
            int from = record.from() - 1;
            int to = record.to() - 1;
            double weight = record.weight();

            // TODO remove these when no longer developing
            if (day > dayLimit) break;
            if (from >= personLimit || to >= personLimit) continue;

            ContactRecord c = ImmutableContactRecord.builder().time(day).from(from).to(to).weight(weight).build();
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
