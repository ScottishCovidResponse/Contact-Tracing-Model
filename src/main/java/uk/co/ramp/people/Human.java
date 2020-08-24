package uk.co.ramp.people;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Gson.TypeAdapters
@Value.Immutable
public interface Human {
  int id();

  int age();

  Gender gender();

  double isolationCompliance();

  double reportingCompliance();

  double health();

  boolean hasApp();
}
