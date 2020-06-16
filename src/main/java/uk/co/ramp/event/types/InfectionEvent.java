package uk.co.ramp.event.types;

import org.immutables.value.Value;
import uk.co.ramp.event.processor.InfectionEventProcessor;

@Value.Immutable
public interface InfectionEvent extends CommonVirusEvent {

    default void applyEventToCase(Case aCase) {
        aCase.setVirusStatus(aCase.virusStatus().transitionTo(this.nextStatus()));
        aCase.setExposedTime(this.exposedTime());
        aCase.setExposedBy(this.exposedBy());
    }

    int exposedBy();

    int exposedTime();

    InfectionEventProcessor eventProcessor();

    default ProcessedEventResult processEvent() {
        return eventProcessor().processEvent(this);
    }
}
