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

  public DiseasePropertiesReader(StandardApi dataPipelineApi) {
    this.dataPipelineApi = dataPipelineApi;
  }

  public DiseaseProperties read() {
    double testPositiveAccuracy = readEstimate("fixed_parameters", "test-positive-accuracy");
    double testNegativeAccuracy = readEstimate("fixed_parameters", "test-negative-accuracy");
    double exposureThreshold = readEstimate("fixed_parameters", "exposure-threshold");
    double exposureProbability4UnitContact =
        readEstimate("fixed_parameters", "exposure-probability-4-unit-contact");
    double exposureExponent = readEstimate("fixed_parameters", "exposure-exponent");
    double randomInfectionRate = readEstimate("fixed_parameters", "random-infection-rate");

    Distribution timeLatentDistribution =
        readDistribution("fixed_parameters", "time-latent-distribution");
    double timeLatentMax = readEstimate("fixed_parameters", "time-latent-max");

    Distribution timeRecoveryAsympDistribution =
        readDistribution("fixed_parameters", "time-recovery-asymp-distribution");
    double timeRecoveryAsympMax = readEstimate("fixed_parameters", "time-recovery-asymp-max");

    Distribution timeRecoverySympDistribution =
        readDistribution("fixed_parameters", "time-recovery-symp-distribution");
    double timeRecoverySympMax = readEstimate("fixed_parameters", "time-recovery-symp-max");

    Distribution timeRecoverySevDistribution =
        readDistribution("fixed_parameters", "time-recovery-sev-distribution");
    double timeRecoverySevMax = readEstimate("fixed_parameters", "time-recovery-sev-max");

    Distribution timeSymptomsOnsetDistribution =
        readDistribution("fixed_parameters", "time-symptoms-onset-distribution");
    double timeSymptomsOnsetMax = readEstimate("fixed_parameters", "time-symptoms-onset-max");

    Distribution timeDeclineDistribution =
        readDistribution("fixed_parameters", "time-decline-distribution");
    double timeDeclineMax = readEstimate("fixed_parameters", "time-decline-max");

    Distribution timeDeathDistribution =
        readDistribution("fixed_parameters", "time-death-distribution");
    double timeDeathMax = readEstimate("fixed_parameters", "time-death-max");

    Distribution timeTestAdministeredDistribution =
        readDistribution("fixed_parameters", "time-test-administered-distribution");
    double timeTestAdministeredMax = readEstimate("fixed_parameters", "time-test-administered-max");

    Distribution timeTestResultDistribution =
        readDistribution("fixed_parameters", "time-test-result-distribution");
    double timeTestResultMax = readEstimate("fixed_parameters", "time-test-result-max");

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
    return dataPipelineApi.readDistribution(dataProduct, component);
  }

  private double readEstimate(String dataProduct, String component) {
    return dataPipelineApi.readEstimate(dataProduct, component).doubleValue();
  }
}
