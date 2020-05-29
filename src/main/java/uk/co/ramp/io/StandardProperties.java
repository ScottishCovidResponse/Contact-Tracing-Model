package uk.co.ramp.io;

import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;

@TypeAdapters
@Immutable
public interface StandardProperties {
    int populationSize();
    int timeLimit();

    int initialExposures();

    int seed();
    boolean steadyState();

}
