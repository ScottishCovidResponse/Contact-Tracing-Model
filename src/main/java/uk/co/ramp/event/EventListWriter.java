package uk.co.ramp.event;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.event.types.ImmutableFormattedEvent;
import uk.co.ramp.io.csv.CsvException;
import uk.co.ramp.io.csv.CsvWriter;

public class EventListWriter {
  public static final String EVENTS_CSV = "events.csv";
  private static final Logger LOGGER = LogManager.getLogger(EventListWriter.class);

  private final FormattedEventFactory formattedEventFactory;
  private final CompletionEventListGroup eventList;
  private final File outputFolder;

  EventListWriter(FormattedEventFactory formattedEventFactory, CompletionEventListGroup eventList,
      File outputFolder) {
    this.formattedEventFactory = formattedEventFactory;
    this.eventList = eventList;
    this.outputFolder = outputFolder;
  }

  public void output() {
    /*
     * Creates a stream of integers from time=0 to last event time
     * then maps each time to events at that time
     * then we pass those events (which are sorted by time) into formattedEventFactory
     * which outputs the data
     */
    var alertEvents =
        IntStream.rangeClosed(0, eventList.lastAlertTime())
            .boxed()
            .map(eventList::getNewAlertEvents)
            .flatMap(Collection::stream)
            .map(formattedEventFactory::create);

    var infectionEvents =
        IntStream.rangeClosed(0, eventList.lastInfectionTime())
            .boxed()
            .map(eventList::getNewInfectionEvents)
            .flatMap(Collection::stream)
            .map(formattedEventFactory::create);

    var virusEvents =
        IntStream.rangeClosed(0, eventList.lastVirusTime())
            .boxed()
            .map(eventList::getNewVirusEvents)
            .flatMap(Collection::stream)
            .map(formattedEventFactory::create);

    // Skipping contact events for now to retain current implementation functionality

    List<ImmutableFormattedEvent> finalList =
        Stream.of(alertEvents, infectionEvents, virusEvents)
            .flatMap(s -> s)
            .sorted(Comparator.comparingInt(ImmutableFormattedEvent::time))
            .collect(Collectors.toList());

    try (Writer writer = new FileWriter(new File(outputFolder, EVENTS_CSV))) {
      new CsvWriter().write(writer, finalList, ImmutableFormattedEvent.class);

    } catch (IOException e) {
      String message = "An error occurred while writing a CSV file";
      LOGGER.error(message);
      throw new CsvException(message, e);
    }
  }
}
