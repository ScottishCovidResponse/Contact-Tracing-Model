package uk.co.ramp.distribution;

import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;
import uk.co.ramp.io.ProgressionDistribution;


@TypeAdapters
@Immutable
public interface Distribution {
    ProgressionDistribution type();

    double mean();

    double max();
}