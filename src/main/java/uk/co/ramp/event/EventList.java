package uk.co.ramp.event;

import java.util.*;
import uk.co.ramp.event.types.Event;

public class EventList<T extends Event> {
  private final Map<Integer, List<T>> map = new HashMap<>();

  // create
  public void addEvent(T e) {
    map.computeIfAbsent(e.time(), k -> new ArrayList<>()).add(e);
  }

  public void addEvents(List<T> events) {
    events.forEach(this::addEvent);
  }

  // read
  public List<T> getForTime(int time) {
    return Collections.unmodifiableList(map.getOrDefault(time, List.of()));
  }

  public int lastEventTime() {
    return map.keySet().stream().mapToInt(i -> i).max().orElseThrow();
  }
}
