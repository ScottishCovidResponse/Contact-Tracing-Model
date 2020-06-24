package uk.co.ramp.event;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.event.types.Event;
import uk.co.ramp.event.types.ImmutableFormattedEvent;
import uk.co.ramp.io.csv.CsvException;
import uk.co.ramp.io.csv.CsvWriter;

public class EventListWriter {
  public static final String EVENTS_CSV = "events.csv";
  private static final Logger LOGGER = LogManager.getLogger(EventListWriter.class);

  private final FormattedEventFactory formattedEventFactory;
  private final EventList<Event> completedEvents;

  EventListWriter(FormattedEventFactory formattedEventFactory, EventList<Event> completedEvents) {
    this.formattedEventFactory = formattedEventFactory;
    this.completedEvents = completedEvents;
  }

  public void output() {
    Map<Integer, List<Event>> completedMap =
        IntStream.rangeClosed(0, completedEvents.lastEventTime())
            .boxed()
            .collect(Collectors.toMap(Function.identity(), completedEvents::getForTime));

    List<Integer> timeStamps = new ArrayList<>(completedMap.keySet());
    timeStamps.sort(Comparator.naturalOrder());
    List<ImmutableFormattedEvent> finalList =
        timeStamps.stream()
            .map(completedMap::get)
            .flatMap(Collection::stream)
            .map(formattedEventFactory::create)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    try (Writer writer = new FileWriter(EVENTS_CSV)) {
      new CsvWriter().write(writer, finalList, ImmutableFormattedEvent.class);

    } catch (IOException e) {
      String message = "An error occurred while writing a CSV file";
      LOGGER.error(message);
      throw new CsvException(message, e);
    }
  }
}
