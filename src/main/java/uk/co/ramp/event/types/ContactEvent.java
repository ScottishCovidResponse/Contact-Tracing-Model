package uk.co.ramp.event.types;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize
@JsonDeserialize
@JsonPropertyOrder({"time", "from", "to", "weight", "label"})
@SuppressWarnings("immutables:from")
public interface ContactEvent extends Event {

  int from();

  int to();

  double weight();

  String label();
}
