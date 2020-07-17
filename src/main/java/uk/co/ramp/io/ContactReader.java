package uk.co.ramp.io;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.ImmutableContactEvent;
import uk.co.ramp.io.csv.CsvReader;
import uk.co.ramp.io.types.StandardProperties;

@Service
public class ContactReader {

  private final StandardProperties properties;
  private final DistributionSampler distributionSampler;

  @Autowired
  public ContactReader(
      StandardProperties standardProperties, DistributionSampler distributionSampler) {
    this.properties = standardProperties;
    this.distributionSampler = distributionSampler;
  }

  public List<ContactEvent> readEvents(Reader reader) throws IOException {

    List<ImmutableContactEvent> contactEvents =
        new CsvReader().read(reader, ImmutableContactEvent.class);

    contactEvents = resampleContacts(contactEvents);

    return contactEvents.stream()
        .filter(event -> event.from() < properties.populationSize())
        .filter(event -> event.to() < properties.populationSize())
        .filter(event -> event.time() <= properties.timeLimit() * properties.timeStepsPerDay())
        .collect(Collectors.toUnmodifiableList());
  }

  List<ImmutableContactEvent> resampleContacts(List<ImmutableContactEvent> contactEvents) {
    int steps = properties.timeStepsPerDay();

    Set<Integer> timeSteps =
        contactEvents.stream().map(ImmutableContactEvent::time).collect(Collectors.toSet());
    List<ImmutableContactEvent> events = new ArrayList<>();
    for (int step : timeSteps) {

      List<ImmutableContactEvent> timeEvents =
          contactEvents.stream().filter(event -> event.time() == step).collect(Collectors.toList());

      int start = step * steps;
      int end = start + steps - 1;
      int[] outcomes = IntStream.rangeClosed(start, end).toArray();

      EnumeratedIntegerDistribution distribution =
          distributionSampler.resampleDays(outcomes, properties.timeStepSpread());

      events.addAll(
          timeEvents.stream()
              .map(i -> ImmutableContactEvent.copyOf(i).withTime(distribution.sample()))
              .collect(Collectors.toList()));
    }
    events.sort(Comparator.comparingInt(ImmutableContactEvent::time));
    return events;
  }
}
