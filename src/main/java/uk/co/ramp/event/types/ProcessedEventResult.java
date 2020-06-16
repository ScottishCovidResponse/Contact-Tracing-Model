package uk.co.ramp.event.types;

import org.immutables.value.Value.Immutable;

import java.util.List;
import java.util.Optional;

@Immutable
public interface ProcessedEventResult {
    List<Event> newEvents();
    Optional<Event> completedEvent();
}
