package uk.co.ramp.event.types;

import org.immutables.value.Value.Immutable;

import java.util.List;

@Immutable
public interface ProcessedEventResult {
    List<AlertEvent> newAlertEvents();
    List<ContactEvent> newContactEvents();
    List<InfectionEvent> newInfectionEvents();
    List<VirusEvent> newVirusEvents();
    List<Event> completedEvents();
}
