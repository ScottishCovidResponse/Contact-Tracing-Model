package uk.co.ramp.statistics.types;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
public interface Infection {

  int seed();

  int infections();
}
