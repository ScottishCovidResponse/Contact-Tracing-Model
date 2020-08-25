package uk.co.ramp.distribution;

import com.google.common.base.Preconditions;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;
import uk.ramp.distribution.Distribution;

@Immutable
public interface BoundedDistribution {
  Distribution distribution();

  double max();

  default int getDistributionValue() {
    if (max() == 0) {
      return 0;
    }

    int value = distribution().getSample().intValue();

    // recursing to avoid artificial peaks at 1 and max
    if (value < 1 || value > max()) {
      value = getDistributionValue();
    }

    return value;
  }

  @Check
  default void check() {
    Preconditions.checkState(max() >= 0, "Max should not be negative");
  }
}
