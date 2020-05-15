package uk.co.ramp.record;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize
@JsonDeserialize
@JsonPropertyOrder({"time", "s", "e", "i", "r"})
public interface SeirRecord {
    int time();
    int s();
    int e();
    int i();
    int r();
}
