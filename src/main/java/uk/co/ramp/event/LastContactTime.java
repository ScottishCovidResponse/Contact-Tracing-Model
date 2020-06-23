package uk.co.ramp.event;

public class LastContactTime {
  private final EventListGroup eventListGroup;

  LastContactTime(EventListGroup eventListGroup) {
    this.eventListGroup = eventListGroup;
  }

  public int get() {
    return eventListGroup.lastContactTime();
  }
}
