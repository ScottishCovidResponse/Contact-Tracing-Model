package uk.co.ramp.event.types;

import org.immutables.value.Value;
import uk.co.ramp.event.processor.InfectionEventProcessor;

@Value.Immutable
public interface InfectionEvent extends CommonVirusEvent {
    int exposedBy();

    int exposedTime();

    InfectionEventProcessor eventProcessor();

    default ProcessedEventResult processEvent() {
        return eventProcessor().processEvent(this);
    }
}
