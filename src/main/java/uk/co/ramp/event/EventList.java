package uk.co.ramp.event;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

  public List<T> getEventsInPeriod(int startTime, int endTime, Predicate<T> filter) {
    /*
     * Creates a stream of integers from start time to end time
     * maps to events at each time step
     * collects all relevant events (already sorted by time) into list
     */
    return IntStream.rangeClosed(startTime, endTime)
        .mapToObj(i -> map.getOrDefault(i, List.of()))
        .flatMap(Collection::stream)
        .filter(filter)
        .collect(Collectors.toUnmodifiableList());
  }
}
