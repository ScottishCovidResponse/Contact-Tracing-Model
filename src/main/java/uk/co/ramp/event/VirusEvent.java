package uk.co.ramp.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import uk.co.ramp.people.VirusStatus;

@Value.Immutable
@JsonSerialize
@JsonDeserialize
public interface VirusEvent extends Event {

    int id();

    VirusStatus oldStatus();

    VirusStatus newStatus();

}