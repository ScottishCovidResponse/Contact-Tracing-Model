package uk.co.ramp.policy.isolation;

import org.immutables.gson.Gson;
import org.immutables.value.Value;
import uk.co.ramp.people.VirusStatus;

@Gson.TypeAdapters
@Value.Immutable
interface VirusStatusIsolationProperty {
  VirusStatus virusStatus();

  IsolationProperty isolationProperty();
}
