package uk.co.ramp.io.types;

import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;
import uk.co.ramp.distribution.ProgressionDistribution;
import uk.co.ramp.utilities.MeanMax;

@TypeAdapters
@Immutable
public interface DiseaseProperties {

    MeanMax timeLatent();

    MeanMax timeRecoveryAsymp();

    MeanMax timeRecoverySymp();

    MeanMax timeRecoverySev();

    MeanMax timeSymptomsOnset();

    MeanMax timeDecline();

    MeanMax timeDeath();

    MeanMax timeTestAdministered();

    MeanMax timeTestResult();

    double testAccuracy();

    double randomInfectionRate();

    double exposureTuning();

    double exposureThreshold();

    ProgressionDistribution progressionDistribution();
}
