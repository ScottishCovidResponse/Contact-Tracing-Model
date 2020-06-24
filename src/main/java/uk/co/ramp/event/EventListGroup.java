package uk.co.ramp.event;

import java.util.*;
import uk.co.ramp.event.types.*;

public class EventListGroup {
  private final EventList<AlertEvent> alertEvents;
  private final EventList<ContactEvent> contactEvents;
  private final EventList<InfectionEvent> infectionEvents;
  private final EventList<VirusEvent> virusEvents;
  private final EventList<Event> completedEvents;

  EventListGroup(
      EventList<AlertEvent> alertEvents,
      EventList<ContactEvent> contactEvents,
      EventList<InfectionEvent> infectionEvents,
      EventList<VirusEvent> virusEvents,
      EventList<Event> completedEvents) {
    this.alertEvents = alertEvents;
    this.contactEvents = contactEvents;
    this.infectionEvents = infectionEvents;
    this.virusEvents = virusEvents;
    this.completedEvents = completedEvents;
  }

  public void addContactEvents(List<ContactEvent> events) {
    contactEvents.addEvents(events);
  }

  public void addInfectionEvents(List<InfectionEvent> events) {
    infectionEvents.addEvents(events);
  }

  public void addAlertEvents(List<AlertEvent> events) {
    alertEvents.addEvents(events);
  }

  public void addVirusEvents(List<VirusEvent> events) {
    virusEvents.addEvents(events);
  }

  public void completed(List<Event> e) {
    completedEvents.addEvents(e);
  }

  public List<ContactEvent> getContactEvents(int time) {
    return Collections.unmodifiableList(contactEvents.getForTime(time));
  }

  public List<AlertEvent> getAlertEvents(int time) {
    return Collections.unmodifiableList(alertEvents.getForTime(time));
  }

  public List<InfectionEvent> getInfectionEvents(int time) {
    return Collections.unmodifiableList(infectionEvents.getForTime(time));
  }

  public List<VirusEvent> getVirusEvents(int time) {
    return Collections.unmodifiableList(virusEvents.getForTime(time));
  }

  public int lastContactTime() {
    return contactEvents.lastEventTime();
  }
}
