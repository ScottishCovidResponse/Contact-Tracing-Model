package uk.co.ramp.event.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.Case;

@Value.Immutable
@JsonSerialize
@JsonDeserialize
public interface AlertEvent extends Event {

    default void applyEventToCase(Case aCase) {
        aCase.setAlertStatus(this.nextStatus());
    }

    int id();

    AlertStatus nextStatus();

    AlertStatus oldStatus();
}
