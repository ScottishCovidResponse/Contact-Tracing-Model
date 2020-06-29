package uk.co.ramp.event;

public class LastContactTime {
  private final CompletionEventListGroup eventList;

  LastContactTime(CompletionEventListGroup eventList) {
    this.eventList = eventList;
  }

  public int get() {
    return eventList.lastContactTime();
  }
}
