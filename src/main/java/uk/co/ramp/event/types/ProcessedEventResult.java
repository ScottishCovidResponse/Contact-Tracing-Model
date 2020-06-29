package uk.co.ramp.event.types;

import java.util.List;
import org.immutables.value.Value.Immutable;

@Immutable
public interface ProcessedEventResult {
  List<AlertEvent> newAlertEvents();

  List<AlertEvent> newCompletedAlertEvents();

  List<ContactEvent> newContactEvents();

  List<ContactEvent> newCompletedContactEvents();

  List<InfectionEvent> newInfectionEvents();

  List<InfectionEvent> newCompletedInfectionEvents();

  List<VirusEvent> newVirusEvents();

  List<VirusEvent> newCompletedVirusEvents();
}
