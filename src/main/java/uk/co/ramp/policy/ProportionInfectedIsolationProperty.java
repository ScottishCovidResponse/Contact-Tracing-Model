package uk.co.ramp.policy;

import org.immutables.gson.Gson;
import org.immutables.value.Value;
import uk.co.ramp.utilities.MinMax;

@Gson.TypeAdapters
@Value.Immutable
interface ProportionInfectedIsolationProperty {
  MinMax proportionInfected();

  IsolationProperty isolationProperty();
}
