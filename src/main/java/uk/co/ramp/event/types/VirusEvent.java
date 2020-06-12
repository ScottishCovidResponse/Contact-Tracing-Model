package uk.co.ramp.event.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import uk.co.ramp.event.processor.VirusEventProcessor;

@Value.Immutable
@JsonSerialize
@JsonDeserialize
public interface VirusEvent extends CommonVirusEvent {
    VirusEventProcessor eventProcessor();

    default ProcessedEventResult processEvent() {
        return eventProcessor().processEvent(this);
    }
}