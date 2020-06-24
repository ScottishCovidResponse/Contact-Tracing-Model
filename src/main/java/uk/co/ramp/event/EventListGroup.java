package uk.co.ramp.event;

import java.util.*;
import uk.co.ramp.event.types.*;

class EventListGroup {
  private final EventList<AlertEvent> alertEvents;
  private final EventList<ContactEvent> contactEvents;
  private final EventList<InfectionEvent> infectionEvents;
  private final EventList<VirusEvent> virusEvents;

  EventListGroup(
      EventList<AlertEvent> alertEvents,
      EventList<ContactEvent> contactEvents,
      EventList<InfectionEvent> infectionEvents,
      EventList<VirusEvent> virusEvents) {
    this.alertEvents = alertEvents;
    this.contactEvents = contactEvents;
    this.infectionEvents = infectionEvents;
    this.virusEvents = virusEvents;
  }

  void addContactEvents(List<ContactEvent> events) {
    contactEvents.addEvents(events);
  }

  void addInfectionEvents(List<InfectionEvent> events) {
    infectionEvents.addEvents(events);
  }

  void addAlertEvents(List<AlertEvent> events) {
    alertEvents.addEvents(events);
  }

  void addVirusEvents(List<VirusEvent> events) {
    virusEvents.addEvents(events);
  }

  List<ContactEvent> getContactEvents(int time) {
    return Collections.unmodifiableList(contactEvents.getForTime(time));
  }

  List<ContactEvent> getContactEventsInPeriod(int start, int end, int id) {
    return contactEvents.getEventsInPeriod(start, end, e -> e.from() == id || e.to() == id);
  }

  List<AlertEvent> getAlertEvents(int time) {
    return Collections.unmodifiableList(alertEvents.getForTime(time));
  }

  List<InfectionEvent> getInfectionEvents(int time) {
    return Collections.unmodifiableList(infectionEvents.getForTime(time));
  }

  List<VirusEvent> getVirusEvents(int time) {
    return Collections.unmodifiableList(virusEvents.getForTime(time));
  }

  int lastContactTime() {
    return contactEvents.lastEventTime();
  }

  int lastInfectionTime() {
    return infectionEvents.lastEventTime();
  }

  int lastAlertTime() {
    return alertEvents.lastEventTime();
  }

  int lastVirusTime() {
    return virusEvents.lastEventTime();
  }
}
