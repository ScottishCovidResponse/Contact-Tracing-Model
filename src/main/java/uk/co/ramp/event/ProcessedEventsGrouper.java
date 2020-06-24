package uk.co.ramp.event;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.Event;
import uk.co.ramp.event.types.ImmutableProcessedEventResult;
import uk.co.ramp.event.types.InfectionEvent;
import uk.co.ramp.event.types.ProcessedEventResult;
import uk.co.ramp.event.types.VirusEvent;

public class ProcessedEventsGrouper {
  private <X extends Event> List<X> mapToList(
      List<ProcessedEventResult> combinedProcessedResults,
      Function<ProcessedEventResult, List<X>> newEventsFunc) {
    return combinedProcessedResults.stream()
        .map(newEventsFunc)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  ProcessedEventResult groupProcessedEventResults(
      List<ProcessedEventResult> processedEventResults) {
    List<AlertEvent> newAlertEvents =
        mapToList(processedEventResults, ProcessedEventResult::newAlertEvents);
    List<ContactEvent> newContactEvents =
        mapToList(processedEventResults, ProcessedEventResult::newContactEvents);
    List<InfectionEvent> newInfectionEvents =
        mapToList(processedEventResults, ProcessedEventResult::newInfectionEvents);
    List<VirusEvent> newVirusEvents =
        mapToList(processedEventResults, ProcessedEventResult::newVirusEvents);
    List<AlertEvent> newCompletedAlertEvents =
        mapToList(processedEventResults, ProcessedEventResult::newCompletedAlertEvents);
    List<ContactEvent> newCompletedContactEvents =
        mapToList(processedEventResults, ProcessedEventResult::newCompletedContactEvents);
    List<InfectionEvent> newCompletedInfectionEvents =
        mapToList(processedEventResults, ProcessedEventResult::newCompletedInfectionEvents);
    List<VirusEvent> newCompletedVirusEvents =
        mapToList(processedEventResults, ProcessedEventResult::newCompletedVirusEvents);

    return ImmutableProcessedEventResult.builder()
        .addAllNewAlertEvents(newAlertEvents)
        .addAllNewContactEvents(newContactEvents)
        .addAllNewInfectionEvents(newInfectionEvents)
        .addAllNewVirusEvents(newVirusEvents)
        .addAllNewCompletedAlertEvents(newCompletedAlertEvents)
        .addAllNewCompletedContactEvents(newCompletedContactEvents)
        .addAllNewCompletedInfectionEvents(newCompletedInfectionEvents)
        .addAllNewCompletedVirusEvents(newCompletedVirusEvents)
        .build();
  }
}
