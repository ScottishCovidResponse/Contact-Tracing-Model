package uk.co.ramp.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import uk.co.ramp.people.AlertStatus;

@Value.Immutable
@JsonSerialize
@JsonDeserialize
public interface AlertEvent extends Event {

    int id();

    AlertStatus newStatus();

    AlertStatus oldStatus();
}
