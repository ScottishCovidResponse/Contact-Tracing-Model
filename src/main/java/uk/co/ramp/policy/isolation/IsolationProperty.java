package uk.co.ramp.policy.isolation;

import com.google.common.base.Preconditions;
import java.util.Optional;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;
import uk.co.ramp.distribution.Distribution;

@Immutable
@TypeAdapters
interface IsolationProperty {
  String id();

  Distribution isolationProbabilityDistribution();

  Optional<Distribution> isolationTimeDistribution();

  Optional<Boolean> overrideCompliance();
  //    IsolationStartTime startTime();
  int priority();

  @Check
  default void check() {
    Preconditions.checkState(!id().isBlank(), "id should not be blank");
  }
}
