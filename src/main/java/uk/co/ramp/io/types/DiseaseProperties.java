package uk.co.ramp.io.types;

import com.google.common.base.Preconditions;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;
import uk.co.ramp.distribution.BoundedDistribution;

@TypeAdapters
@Immutable
public interface DiseaseProperties {

  BoundedDistribution timeLatent();

  BoundedDistribution timeRecoveryAsymp();

  BoundedDistribution timeRecoverySymp();

  BoundedDistribution timeRecoverySev();

  BoundedDistribution timeSymptomsOnset();

  BoundedDistribution timeDecline();

  BoundedDistribution timeDeath();

  BoundedDistribution timeTestAdministered();

  BoundedDistribution timeTestResult();

  double testPositiveAccuracy();

  double testNegativeAccuracy();

  double randomInfectionRate();

  double exposureThreshold();

  double exposureProbability4UnitContact();

  double exposureExponent();

  @Check
  default void check() {
    Preconditions.checkState(
        testPositiveAccuracy() >= 0 && testPositiveAccuracy() <= 1,
        "Test accuracy should be between 0 and 1");
    Preconditions.checkState(
        testNegativeAccuracy() >= 0 && testNegativeAccuracy() <= 1,
        "Test accuracy should be between 0 and 1");
    Preconditions.checkState(
        randomInfectionRate() >= 0 && randomInfectionRate() <= 1,
        "Random infection rate should be between 0 and 1");
    Preconditions.checkState(
        exposureExponent() > 0,
        "Power exponent to determine the sensitivity of exposure probability to contact weight  should be positive");
    Preconditions.checkState(
        exposureProbability4UnitContact() > 0 && exposureProbability4UnitContact() < 1,
        "Exposure probability when contact weight is one should be between 0 and 1");
  }
}
