package uk.co.ramp.event.types;

import org.immutables.value.Value;
import uk.co.ramp.people.Case;

@Value.Immutable
public interface InfectionEvent extends CommonVirusEvent {

    default void applyEventToCase(Case aCase) {
        aCase.setVirusStatus(aCase.virusStatus().transitionTo(this.nextStatus()));
    }

    int exposedBy();

    int exposedTime();
}
