package uk.co.ramp.io.types;

import com.google.common.base.Preconditions;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Check;
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

  @Check
  default void check() {
    Preconditions.checkState(
        testAccuracy() >= 0 && testAccuracy() <= 1, "Test accuracy should be between 0 and 1");
    Preconditions.checkState(
        randomInfectionRate() >= 0 && randomInfectionRate() <= 1,
        "Random infection rate should be between 0 and 1");
    Preconditions.checkState(exposureTuning() >= 0, "Exposure tuning value should be positive");
  }
}
