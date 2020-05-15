package uk.co.ramp.io;

import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;

@TypeAdapters
@Immutable
public interface DiseaseProperties {
    double meanTimeToInfected();
    double meanTimeToRecovered();
    double randomInfectionRate();
    double exposureTuning();
    ProgressionDistribution progressionDistribution();
}
