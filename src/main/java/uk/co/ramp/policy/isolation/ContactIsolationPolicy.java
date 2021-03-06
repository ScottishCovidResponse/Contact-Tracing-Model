package uk.co.ramp.policy.isolation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;

class ContactIsolationPolicy implements IsolationPolicy {
  private static final Logger LOGGER = LogManager.getLogger(ContactIsolationPolicy.class);

  private final SingleCaseIsolationPolicy singleCaseIsolationPolicy;
  private final DiseaseProperties diseaseProperties;

  ContactIsolationPolicy(
      SingleCaseIsolationPolicy singleCaseIsolationPolicy, DiseaseProperties diseaseProperties) {
    this.singleCaseIsolationPolicy = singleCaseIsolationPolicy;
    this.diseaseProperties = diseaseProperties;
  }

  @Override
  public boolean isContactIsolated(
      Case caseA,
      Case caseB,
      double contactWeight,
      double actualInfectedProportion,
      int currentTime) {
    boolean isCaseAInIsolation =
        singleCaseIsolationPolicy.isIndividualInIsolation(
            caseA, actualInfectedProportion, currentTime);
    boolean isCaseBInIsolation =
        singleCaseIsolationPolicy.isIndividualInIsolation(
            caseB, actualInfectedProportion, currentTime);

    boolean weakContact =
        contactWeight
            < diseaseProperties
                .exposureThreshold(); // TODO implement and use regional lockdown policy

    boolean shouldIsolate = (isCaseAInIsolation || isCaseBInIsolation) && weakContact;
    if (shouldIsolate) {
      LOGGER.trace(
          "A in isolation: {}   B in isolation: {}   weight: {}   A compliance: {}   B compliance: {}",
          isCaseAInIsolation,
          isCaseBInIsolation,
          contactWeight,
          caseA.isolationCompliance(),
          caseB.isolationCompliance());
    }
    return shouldIsolate;
  }
}
