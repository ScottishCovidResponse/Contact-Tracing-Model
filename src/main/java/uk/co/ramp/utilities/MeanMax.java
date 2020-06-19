package uk.co.ramp.utilities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;

@TypeAdapters
@JsonSerialize
@JsonDeserialize
@Immutable
public interface MeanMax {
    int mean();

    int max();

    @Check
    default void check() {
        Preconditions.checkState(mean() >= 0, "Mean should not be negative");
        Preconditions.checkState(max() >= 0, "Max should not be negative");
        Preconditions.checkState(mean() <= max(), "Mean should be less than or equal to max");
    }
}
