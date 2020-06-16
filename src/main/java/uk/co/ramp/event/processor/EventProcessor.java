package uk.co.ramp.event.processor;

import uk.co.ramp.event.types.Event;
import uk.co.ramp.event.types.ProcessedEventResult;

public interface EventProcessor<T extends Event> {
    ProcessedEventResult processEvent(T event);
}
