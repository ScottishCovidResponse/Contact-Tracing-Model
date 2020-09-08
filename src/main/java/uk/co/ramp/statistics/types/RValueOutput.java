package uk.co.ramp.statistics.types;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize
@JsonDeserialize
@Gson.TypeAdapters
@JsonPropertyOrder({"time", "newInfectors", "newInfections", "r", "sevenDayAverageR"})
public interface RValueOutput {

  double time();

  int newInfectors();

  int newInfections();

  double r();

  double sevenDayAverageR();
}
