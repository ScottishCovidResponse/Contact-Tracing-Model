package uk.co.ramp.policy.alert;

import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.VirusStatus.DEAD;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import uk.co.ramp.Population;
import uk.co.ramp.event.CompletionEventListGroup;
import uk.co.ramp.statistics.StatisticsRecorder;

public class AlertContactTracer {
  private final TracingPolicy tracingPolicy;
  private final CompletionEventListGroup eventList;
  private final Population population;
  private final StatisticsRecorder statisticsRecorder;

  AlertContactTracer(
      TracingPolicy tracingPolicy,
      CompletionEventListGroup eventList,
      Population population,
      StatisticsRecorder statisticsRecorder) {
    this.tracingPolicy = tracingPolicy;
    this.eventList = eventList;
    this.population = population;
    this.statisticsRecorder = statisticsRecorder;
  }

  Set<Integer> traceRecentContacts(int startTime, int currentTime, int personId) {
    return traceRecentContacts(startTime, currentTime, personId, 1);
  }

  private Set<Integer> traceRecentContacts(
      int startTime, int currentTime, int personId, int currentLevel) {
    int noOfLevelsToTrace = tracingPolicy.noOfTracingLevels();
    if (currentLevel > noOfLevelsToTrace) {
      return Set.of();
    }

    Set<Integer> currentLevelContactsTrace =
        traceSingleLevelOfRecentContacts(startTime, currentTime, personId);
    Set<Integer> nextLevelContactsTrace =
        currentLevelContactsTrace.stream()
            .map(id -> traceRecentContacts(startTime, currentTime, id, currentLevel + 1))
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

    statisticsRecorder.recordContactsTraced(currentTime, currentLevelContactsTrace.size());

    return Stream.of(currentLevelContactsTrace, nextLevelContactsTrace)
        .flatMap(Collection::stream)
        .filter(id -> id != personId)
        .filter(id -> population.hasApp(personId))
        .collect(Collectors.toSet());
  }

  private Set<Integer> traceSingleLevelOfRecentContacts(
      int startTime, int currentTime, int personId) {
    return eventList.getCompletedContactEventsInPeriod(startTime, currentTime, personId).stream()
        .flatMapToInt(e -> IntStream.of(e.from(), e.to()))
        .distinct()
        .filter(id -> id != personId)
        .filter(id -> population.getAlertStatus(id) == NONE)
        .filter(id -> population.getVirusStatus(id) != DEAD)
        .boxed()
        .collect(Collectors.toSet());
  }
}
