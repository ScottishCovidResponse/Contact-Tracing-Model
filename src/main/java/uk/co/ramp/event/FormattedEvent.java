package uk.co.ramp.event;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize
@JsonDeserialize
@JsonPropertyOrder({"time", "eventType", "id", "newStatus", "additional"})
public interface FormattedEvent {

    int time();

    String eventType();

    int id();

    String newStatus();

    String additionalInfo();

}
