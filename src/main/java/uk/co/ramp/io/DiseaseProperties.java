package uk.co.ramp.io;

import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;

@TypeAdapters
@Immutable
public interface DiseaseProperties {

    double meanTimeToInfectious();

    double meanTimeToInfected();

    double meanTimeToFinalState();

    double maxTimeToInfectious();

    double maxTimeToInfected();

    double maxTimeToFinalState();

    double randomInfectionRate();

    double exposureTuning();

    ProgressionDistribution progressionDistribution();

}
