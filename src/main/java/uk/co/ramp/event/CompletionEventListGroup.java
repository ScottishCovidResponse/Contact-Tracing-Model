package uk.co.ramp.event;

import java.util.List;
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.InfectionEvent;
import uk.co.ramp.event.types.VirusEvent;

public class CompletionEventListGroup {
  private final EventListGroup newEvents;
  private final EventListGroup completedEvents;

  CompletionEventListGroup(EventListGroup newEvents, EventListGroup completedEvents) {
    this.newEvents = newEvents;
    this.completedEvents = completedEvents;
  }

  public void addNewContactEvents(List<ContactEvent> events) {
    newEvents.addContactEvents(events);
  }

  void addNewInfectionEvents(List<InfectionEvent> events) {
    newEvents.addInfectionEvents(events);
  }

  void addNewAlertEvents(List<AlertEvent> events) {
    newEvents.addAlertEvents(events);
  }

  void addNewVirusEvents(List<VirusEvent> events) {
    newEvents.addVirusEvents(events);
  }

  List<ContactEvent> getNewContactEvents(int time) {
    return newEvents.getContactEvents(time);
  }

  public List<ContactEvent> getCompletedContactEventsInPeriod(int start, int end, int id) {
    return completedEvents.getContactEventsInPeriod(start, end, id);
  }

  List<AlertEvent> getNewAlertEvents(int time) {
    return newEvents.getAlertEvents(time);
  }

  List<InfectionEvent> getNewInfectionEvents(int time) {
    return newEvents.getInfectionEvents(time);
  }

  List<VirusEvent> getNewVirusEvents(int time) {
    return newEvents.getVirusEvents(time);
  }

  void addCompletedContactEvents(List<ContactEvent> events) {
    completedEvents.addContactEvents(events);
  }

  void addCompletedInfectionEvents(List<InfectionEvent> events) {
    completedEvents.addInfectionEvents(events);
  }

  void addCompletedAlertEvents(List<AlertEvent> events) {
    completedEvents.addAlertEvents(events);
  }

  void addCompletedVirusEvents(List<VirusEvent> events) {
    completedEvents.addVirusEvents(events);
  }

  public List<ContactEvent> getCompletedContactEvents(int time) {
    return completedEvents.getContactEvents(time);
  }

  public List<AlertEvent> getCompletedAlertEvents(int time) {
    return completedEvents.getAlertEvents(time);
  }

  public List<InfectionEvent> getCompletedInfectionEvents(int time) {
    return completedEvents.getInfectionEvents(time);
  }

  public List<VirusEvent> getCompletedVirusEvents(int time) {
    return completedEvents.getVirusEvents(time);
  }

  public int lastContactTime() {
    return newEvents.lastContactTime();
  }

  public int lastInfectionTime() {
    return newEvents.lastInfectionTime();
  }

  public int lastAlertTime() {
    return newEvents.lastAlertTime();
  }

  public int lastVirusTime() {
    return newEvents.lastVirusTime();
  }
}
