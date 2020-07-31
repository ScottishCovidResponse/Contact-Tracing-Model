package uk.co.ramp.policy.isolation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import uk.co.ramp.people.AlertStatus;

@JsonSerialize
@JsonDeserialize
@Value.Immutable
interface AlertStatusIsolationProperty {
  AlertStatus alertStatus();

  ImmutableIsolationProperty isolationProperty();
}
