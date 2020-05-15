package uk.co.ramp.utilities;

import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;

@TypeAdapters
@Immutable
public abstract class MinMax {
    public abstract int min();
    public abstract int max();

    public static MinMax of(int a, int b) {
        return ImmutableMinMax.builder()
                .min(Math.min(a, b))
                .max(Math.max(a, b))
                .build();
    }
}
