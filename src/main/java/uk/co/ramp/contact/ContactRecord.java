package uk.co.ramp.contact;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize
@JsonDeserialize
@JsonPropertyOrder({"time", "from", "to", "weight"})
@SuppressWarnings("immutables:from")
public interface ContactRecord {
    int time();
    int from();
    int to();
    double weight();
}
