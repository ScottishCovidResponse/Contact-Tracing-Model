package uk.co.ramp.policy.isolation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import uk.co.ramp.utilities.ImmutableMinMax;

@JsonSerialize
@JsonDeserialize
@Value.Immutable
interface ProportionInfectedIsolationProperty {
  ImmutableMinMax proportionInfected();

  ImmutableIsolationProperty isolationProperty();
}
