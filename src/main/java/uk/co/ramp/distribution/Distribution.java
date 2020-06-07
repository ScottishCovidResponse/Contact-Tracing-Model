package uk.co.ramp.distribution;

import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;

@TypeAdapters
@Immutable
public interface Distribution {
    ProgressionDistribution type();
    double mean();
    double max();
}
