package uk.co.ramp.policy.isolation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import java.util.Optional;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;
import uk.co.ramp.distribution.BoundedDistribution;

@Immutable
@JsonSerialize
@JsonDeserialize
interface IsolationProperty {
  String id();

  BoundedDistribution isolationProbabilityDistribution();

  Optional<BoundedDistribution> isolationTimeDistribution();

  Optional<Boolean> overrideComplianceAndForcePolicy();

  Optional<IsolationStartTimeType> startOfIsolationTime();

  int priority();

  @Check
  default void check() {
    Preconditions.checkState(!id().isBlank(), "id should not be blank");
  }
}
