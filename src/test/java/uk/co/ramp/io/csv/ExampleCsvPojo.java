package uk.co.ramp.io.csv;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import static org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize
@JsonDeserialize
@JsonPropertyOrder({"a", "b", "c"})
public interface ExampleCsvPojo {
    int a();
    int b();
    int c();
}