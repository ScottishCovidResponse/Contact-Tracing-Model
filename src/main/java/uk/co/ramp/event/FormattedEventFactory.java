package uk.co.ramp.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import uk.co.ramp.event.types.*;
import uk.co.ramp.people.Case;

@Service
public class FormattedEventFactory {

  private static final Logger LOGGER = LogManager.getLogger(FormattedEventFactory.class);

  public ImmutableFormattedEvent create(Event event) {

    if (event instanceof VirusEvent) {
      return create((VirusEvent) event);
    } else if (event instanceof InfectionEvent) {
      return create((InfectionEvent) event);
    } else if (event instanceof AlertEvent) {
      return create((AlertEvent) event);
    } else if (event instanceof ContactEvent) {
      // todo do we want to print these all out as they are an input?
      return null;
    } else if (event instanceof PolicyEvent) {
      return create((PolicyEvent) event);
    } else {
      String message = "Unknown Event Type: " + event.getClass().getSimpleName();
      LOGGER.error(message);
      throw new EventException(message);
    }
  }

  ImmutableFormattedEvent create(VirusEvent event) {
    return ImmutableFormattedEvent.builder()
        .time(event.time())
        .eventType("VirusEvent")
        .id(event.id())
        .newStatus(event.nextStatus().toString())
        .additionalInfo("Old Status : " + event.oldStatus())
        .build();
  }

  ImmutableFormattedEvent create(InfectionEvent event) {

    String exposedBy;
    if (event.exposedBy() == Case.getInitial()) {
      exposedBy = "This case was an initial infection";
    } else if (event.exposedBy() == Case.getRandomInfection()) {
      exposedBy = "This case was a random infection";
    } else {
      exposedBy =
          "This case was due to contact with "
              + event.exposedBy()
              + " at time = "
              + event.exposedTime();
    }

    return ImmutableFormattedEvent.builder()
        .time(event.time())
        .eventType("InfectionEvent")
        .id(event.id())
        .newStatus(event.nextStatus().toString())
        .additionalInfo(exposedBy)
        .build();
  }

  ImmutableFormattedEvent create(AlertEvent event) {
    return ImmutableFormattedEvent.builder()
        .time(event.time())
        .eventType("AlertEvent")
        .id(event.id())
        .newStatus(event.nextStatus().toString())
        .additionalInfo("")
        .build();
  }

  ImmutableFormattedEvent create(PolicyEvent event) {
    return ImmutableFormattedEvent.builder()
        .time(event.time())
        .eventType("PolicyEvent")
        .id(0)
        .newStatus("")
        .additionalInfo("TODO will need to fill out")
        .build();
  }

  ImmutableFormattedEvent create(ContactEvent event) {
    return ImmutableFormattedEvent.builder()
        .time(event.time())
        .eventType("ContactEvent")
        .id(event.to())
        .newStatus(event.label())
        .additionalInfo("contact weight is " + event.weight())
        .build();
  }
}
