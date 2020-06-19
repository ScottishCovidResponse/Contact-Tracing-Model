package uk.co.ramp.event.types;

public interface EventProcessor<T extends Event> {
    ProcessedEventResult processEvent(T event);
}
