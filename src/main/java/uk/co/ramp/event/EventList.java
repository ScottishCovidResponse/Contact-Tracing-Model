package uk.co.ramp.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.Event;
import uk.co.ramp.event.types.ImmutableFormattedEvent;
import uk.co.ramp.io.csv.CsvException;
import uk.co.ramp.io.csv.CsvWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class EventList {

    public static final String EVENTS_CSV = "events.csv";
    private final Map<Integer, List<Event>> map = new HashMap<>();
    private final Map<Integer, List<Event>> completedMap = new HashMap<>();
    private final FormattedEventFactory formattedEventFactory;

    private static final Logger LOGGER = LogManager.getLogger(EventList.class);

    @Autowired
    public EventList(FormattedEventFactory formattedEventFactory) {
        this.formattedEventFactory = formattedEventFactory;
    }

    //create
    public void addEvent(Event e) {
        map.computeIfAbsent(e.time(), k -> new ArrayList<>()).add(e);
    }

    public void addEvents(List<Event> events) {
        events.forEach(this::addEvent);
    }

    public void addEvents(Map<Integer, List<ContactEvent>> readEvents) {

        for (Map.Entry<Integer, List<ContactEvent>> entry : readEvents.entrySet()) {
            bulkAdd(entry.getKey(), entry.getValue());
        }
    }

    private void bulkAdd(int time, List<ContactEvent> events) {
        map.computeIfAbsent(time, k -> new ArrayList<>()).addAll(events);
    }

    public void completed(Event e) {
        completedMap.computeIfAbsent(e.time(), k -> new ArrayList<>()).add(e);
    }


    //read
    List<Event> getForTime(int time) {
        return map.getOrDefault(time, new ArrayList<>());
    }

    public Map<Integer, List<Event>> getMap() {
        return map;
    }

    public void output() {

        List<Integer> timeStamps = new ArrayList<>(completedMap.keySet());
        timeStamps.sort(Comparator.naturalOrder());
        List<ImmutableFormattedEvent> finalList = timeStamps.stream().map(completedMap::get).flatMap(Collection::stream).map(formattedEventFactory::create).filter(Objects::nonNull).collect(Collectors.toList());

        try (Writer writer = new FileWriter(EVENTS_CSV)) {
            new CsvWriter().write(writer, finalList, ImmutableFormattedEvent.class);

        } catch (IOException e) {
            String message = "An error occurred while writing a CSV file";
            LOGGER.error(message);
            throw new CsvException(message, e);
        }

    }

    public Optional<Integer> lastContactTime() {
        return map.keySet().stream().max(Comparator.naturalOrder());
    }

}
