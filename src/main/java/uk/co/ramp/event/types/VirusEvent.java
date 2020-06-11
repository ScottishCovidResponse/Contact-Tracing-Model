package uk.co.ramp.event.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import uk.co.ramp.people.Case;

@Value.Immutable
@JsonSerialize
@JsonDeserialize
public interface VirusEvent extends CommonVirusEvent {

    default void applyEventToCase(Case aCase) {
        aCase.setVirusStatus(aCase.virusStatus().transitionTo(this.nextStatus()));
    }

}