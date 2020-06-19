package uk.co.ramp.distribution;

import com.google.common.base.Preconditions;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;

@TypeAdapters
@Immutable
public interface Distribution {
    ProgressionDistribution type();
    double mean();
    double max();

    @Check
    default void check() {
        Preconditions.checkState(mean() <= max(), "Mean should be less than or equal to max");
        Preconditions.checkState(mean() >= 0, "Mean should not be negative");
        Preconditions.checkState(max() >= 0, "Max should not be negative");
    }
}
