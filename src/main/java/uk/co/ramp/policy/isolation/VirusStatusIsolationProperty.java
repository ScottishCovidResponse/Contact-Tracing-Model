package uk.co.ramp.policy.isolation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import uk.co.ramp.people.VirusStatus;

@JsonSerialize
@JsonDeserialize
@Value.Immutable
interface VirusStatusIsolationProperty {
  VirusStatus virusStatus();

  ImmutableIsolationProperty isolationProperty();
}
