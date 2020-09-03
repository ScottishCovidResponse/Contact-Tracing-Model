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
  private final String fixed_parameters = "fixed_parameters";

  public DiseasePropertiesReader(StandardApi dataPipelineApi) {
    this.dataPipelineApi = dataPipelineApi;
  }

  public DiseaseProperties read() {

    double testPositiveAccuracy = readEstimate("test-positive-accuracy");
    double testNegativeAccuracy = readEstimate("test-negative-accuracy");
    double exposureThreshold = readEstimate("exposure-threshold");
    double exposureProbability4UnitContact = readEstimate("exposure-probability-4-unit-contact");
    double exposureExponent = readEstimate("exposure-exponent");
    double randomInfectionRate = readEstimate("random-infection-rate");

    Distribution timeLatentDistribution = readDistribution("time-latent-distribution");
    double timeLatentMax = readEstimate("time-latent-max");

    Distribution timeRecoveryAsympDistribution =
        readDistribution("time-recovery-asymp-distribution");
    double timeRecoveryAsympMax = readEstimate("time-recovery-asymp-max");

    Distribution timeRecoverySympDistribution = readDistribution("time-recovery-symp-distribution");
    double timeRecoverySympMax = readEstimate("time-recovery-symp-max");

    Distribution timeRecoverySevDistribution = readDistribution("time-recovery-sev-distribution");
    double timeRecoverySevMax = readEstimate("time-recovery-sev-max");

    Distribution timeSymptomsOnsetDistribution =
        readDistribution("time-symptoms-onset-distribution");
    double timeSymptomsOnsetMax = readEstimate("time-symptoms-onset-max");

    Distribution timeDeclineDistribution = readDistribution("time-decline-distribution");
    double timeDeclineMax = readEstimate("time-decline-max");

    Distribution timeDeathDistribution = readDistribution("time-death-distribution");
    double timeDeathMax = readEstimate("time-death-max");

    Distribution timeTestAdministeredDistribution =
        readDistribution("time-test-administered-distribution");
    double timeTestAdministeredMax = readEstimate("time-test-administered-max");

    Distribution timeTestResultDistribution = readDistribution("time-test-result-distribution");
    double timeTestResultMax = readEstimate("time-test-result-max");

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

  private Distribution readDistribution(String component) {
    return dataPipelineApi.readDistribution(fixed_parameters, component);
  }

  private double readEstimate(String component) {
    return dataPipelineApi.readEstimate(fixed_parameters, component).doubleValue();
  }
}
