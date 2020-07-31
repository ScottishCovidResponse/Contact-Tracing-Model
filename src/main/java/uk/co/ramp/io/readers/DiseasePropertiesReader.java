package uk.co.ramp.io.readers;

import uk.co.ramp.distribution.BoundedDistribution;
import uk.co.ramp.distribution.ImmutableBoundedDistribution;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.ImmutableDiseaseProperties;
import uk.ramp.api.StandardApi;
import uk.ramp.distribution.Distribution;
import uk.ramp.distribution.ImmutableDistribution;

public class DiseasePropertiesReader {
  private final StandardApi dataPipelineApi;
  private final long seed;
  private final boolean isSeeded;

  public DiseasePropertiesReader(StandardApi dataPipelineApi, long seed) {
    this.dataPipelineApi = dataPipelineApi;
    this.seed = seed;
    this.isSeeded = true;
  }

  public DiseasePropertiesReader(StandardApi dataPipelineApi) {
    this.dataPipelineApi = dataPipelineApi;
    this.seed = 0;
    this.isSeeded = false;
  }

  public DiseaseProperties read() {
    double testPositiveAccuracy = readEstimate("CTM", "test-positive-accuracy");
    double testNegativeAccuracy = readEstimate("CTM", "test-negative-accuracy");
    double exposureThreshold = readEstimate("CTM", "exposure-threshold");
    double exposureProbability4UnitContact =
        readEstimate("CTM", "exposure-probability-4-unit-contact");
    double exposureExponent = readEstimate("CTM", "exposure-exponent");
    double randomInfectionRate = readEstimate("CTM", "random-infection-rate");

    Distribution timeLatentDistribution = readDistribution("CTM", "time-latent-distribution");
    double timeLatentMax = readEstimate("CTM", "time-latent-max");

    Distribution timeRecoveryAsympDistribution =
        readDistribution("CTM", "time-recovery-asymp-distribution");
    double timeRecoveryAsympMax = readEstimate("CTM", "time-recovery-asymp-max");

    Distribution timeRecoverySympDistribution =
        readDistribution("CTM", "time-recovery-symp-distribution");
    double timeRecoverySympMax = readEstimate("CTM", "time-recovery-symp-max");

    Distribution timeRecoverySevDistribution =
        readDistribution("CTM", "time-recovery-sev-distribution");
    double timeRecoverySevMax = readEstimate("CTM", "time-recovery-sev-max");

    Distribution timeSymptomsOnsetDistribution =
        readDistribution("CTM", "time-symptoms-onset-distribution");
    double timeSymptomsOnsetMax = readEstimate("CTM", "time-symptoms-onset-max");

    Distribution timeDeclineDistribution = readDistribution("CTM", "time-decline-distribution");
    double timeDeclineMax = readEstimate("CTM", "time-decline-max");

    Distribution timeDeathDistribution = readDistribution("CTM", "time-death-distribution");
    double timeDeathMax = readEstimate("CTM", "time-death-max");

    Distribution timeTestAdministeredDistribution =
        readDistribution("CTM", "time-test-administered-distribution");
    double timeTestAdministeredMax = readEstimate("CTM", "time-test-administered-max");

    Distribution timeTestResultDistribution =
        readDistribution("CTM", "time-test-result-distribution");
    double timeTestResultMax = readEstimate("CTM", "time-test-result-max");

    return ImmutableDiseaseProperties.builder()
        .testPositiveAccuracy(testPositiveAccuracy)
        .testNegativeAccuracy(testNegativeAccuracy)
        .exposureThreshold(exposureThreshold)
        .exposureProbability4UnitContact(exposureProbability4UnitContact)
        .exposureExponent(exposureExponent)
        .randomInfectionRate(randomInfectionRate)
        .timeLatent(boundedDistribution(timeLatentDistribution, timeLatentMax))
        .timeRecoveryAsymp(boundedDistribution(timeRecoveryAsympDistribution, timeRecoveryAsympMax))
        .timeRecoverySymp(boundedDistribution(timeRecoverySympDistribution, timeRecoverySympMax))
        .timeRecoverySev(boundedDistribution(timeRecoverySevDistribution, timeRecoverySevMax))
        .timeSymptomsOnset(boundedDistribution(timeSymptomsOnsetDistribution, timeSymptomsOnsetMax))
        .timeDecline(boundedDistribution(timeDeclineDistribution, timeDeclineMax))
        .timeDeath(boundedDistribution(timeDeathDistribution, timeDeathMax))
        .timeTestAdministered(
            boundedDistribution(timeTestAdministeredDistribution, timeTestAdministeredMax))
        .timeTestResult(boundedDistribution(timeTestResultDistribution, timeTestResultMax))
        .build();
  }

  private BoundedDistribution boundedDistribution(Distribution dist, double max) {
    return ImmutableBoundedDistribution.builder()
        .distribution(ImmutableDistribution.copyOf(dist))
        .max(max)
        .build();
  }

  private Distribution readDistribution(String dataProduct, String component) {
    Distribution dist = dataPipelineApi.readDistribution(dataProduct, component);
    if (isSeeded) {
      dist.underlyingDistribution().reseedRandomGenerator(seed);
    }
    return dist;
  }

  private double readEstimate(String dataProduct, String component) {
    return dataPipelineApi.readEstimate(dataProduct, component).doubleValue();
  }
}
