package uk.co.ramp.io;

import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;

@TypeAdapters
@Immutable
public interface DiseaseProperties {
    double meanTimeToInfectious();

    double maxTimeToInfectious();

    double meanTimeToInfected();

    double maxTimeToInfected();

    double meanTimeToFinalState();

    double maxTimeToFinalState();

    double meanTestTime();

    double maxTestTime();

    double testAccuracy();

    double randomInfectionRate();

    double exposureTuning();

    double exposureThreshold();

    ProgressionDistribution progressionDistribution();
}
