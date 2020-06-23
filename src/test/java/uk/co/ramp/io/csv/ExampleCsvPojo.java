package uk.co.ramp.io.csv;

import static org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Immutable
@JsonSerialize
@JsonDeserialize
@JsonPropertyOrder({"a", "b", "c"})
public interface ExampleCsvPojo {
  int a();

  int b();

  int c();
}
