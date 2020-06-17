package uk.co.ramp.io.types;

import com.google.common.base.Preconditions;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;

@TypeAdapters
@Immutable
public interface StandardProperties {
    int populationSize();
    int timeLimit();

    int initialExposures();

    int seed();
    boolean steadyState();

    @Check
    default void check() {
        Preconditions.checkState(populationSize() > 0, "Population size should be greater than 0");
        Preconditions.checkState(timeLimit() >= 0, "Time limit should not be negative");
        Preconditions.checkState(initialExposures() >= 0, "Initial exposures should not be negative");
    }
}
