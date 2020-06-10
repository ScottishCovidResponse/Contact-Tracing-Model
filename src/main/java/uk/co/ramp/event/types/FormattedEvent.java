package uk.co.ramp.event.types;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize
@JsonDeserialize
@JsonPropertyOrder({"time", "eventType", "id", "newStatus", "additionalInfo"})
public interface FormattedEvent {

    int time();

    String eventType();

    int id();

    String newStatus();

    String additionalInfo();

}
