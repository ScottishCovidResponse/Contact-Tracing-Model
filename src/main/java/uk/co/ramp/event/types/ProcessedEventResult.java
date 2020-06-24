package uk.co.ramp.event.types;

import java.util.List;
import org.immutables.value.Value.Immutable;

@Immutable
public interface ProcessedEventResult {
  List<AlertEvent> newAlertEvents();

  List<ContactEvent> newContactEvents();

  List<InfectionEvent> newInfectionEvents();

  List<VirusEvent> newVirusEvents();

  List<Event> completedEvents();
}
