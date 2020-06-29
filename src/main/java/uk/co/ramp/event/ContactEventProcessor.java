package uk.co.ramp.event;

import static uk.co.ramp.people.VirusStatus.EXPOSED;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

import java.util.Optional;
import org.apache.commons.math3.util.FastMath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.policy.isolation.IsolationPolicy;

public class ContactEventProcessor implements EventProcessor<ContactEvent> {
  private static final Logger LOGGER = LogManager.getLogger(ContactEventProcessor.class);

  private final Population population;
  private final DiseaseProperties diseaseProperties;
  private final DistributionSampler distributionSampler;
  private final IsolationPolicy isolationPolicy;

  public ContactEventProcessor(
      Population population,
      DiseaseProperties diseaseProperties,
      DistributionSampler distributionSampler,
      IsolationPolicy isolationPolicy) {
    this.population = population;
    this.diseaseProperties = diseaseProperties;
    this.distributionSampler = distributionSampler;
    this.isolationPolicy = isolationPolicy;
  }

  @Override
  public ProcessedEventResult processEvent(ContactEvent event) {
    Optional<InfectionEvent> newEvent =
        evaluateContact(event, proportionOfPopulationInfectious(event.time()));
    return newEvent
        .map(
            e ->
                ImmutableProcessedEventResult.builder()
                    .addNewInfectionEvents(e)
                    .addNewCompletedContactEvents(event)
                    .build())
        .orElseGet(() -> ImmutableProcessedEventResult.builder().build());
  }

  private double proportionOfPopulationInfectious(int time) {
    return population.proportionInfectious(time);
  }

  private boolean isContactIsolated(
      ContactEvent contact, Case caseA, Case caseB, double proportionInfectious, int currentTime) {
    return isolationPolicy.isContactIsolated(
        caseA, caseB, contact.weight(), proportionInfectious, currentTime);
  }

  Optional<InfectionEvent> evaluateContact(ContactEvent contacts, double proportionInfectious) {
    int time = contacts.time();
    Case potentialSpreader = population.get(contacts.to());
    Case victim = population.get(contacts.from());

    boolean shouldIsolateContact =
        isContactIsolated(contacts, potentialSpreader, victim, proportionInfectious, time);

    if (shouldIsolateContact) {
      LOGGER.trace("Skipping contact due to isolation");
      return Optional.empty();
    }

    if (potentialSpreader.virusStatus() != victim.virusStatus()) {
      return evaluateExposures(contacts, time);
    }

    return Optional.empty();
  }

  /**
   * Randomly generate an exposure event, based on contact weight between two persons.
   *
   * <p>For contact weight C >= 0, probability of exposure is given by P(exposure=1|C) =
   * Sigmoid(bias + exponent * ln(C)) = ( exp(bias) * C^{exponent} ) / (1 + exp(bias) *
   * C^{exponent}),
   *
   * <p>where exponent > 0 and Sigmoid(x) = 1 / (1 + exp(-x)).
   *
   * <p>As long as exponent>0, this formula satisfies P(exposure=1|C=0)==0, i.e., contact weight ==
   * 0, which corresponds to no contact, is guaranteed to unchange the status. This is a necessary
   * boundary condition, because lack of contact record is implicitly assumed to be contact weight
   * == 0.
   *
   * <p>The bias and exponent parameters control level of the probability and sensitivity of the
   * probability to the contact weight, respectively.
   *
   * <p>For an intuitive parameter specification in {@link DiseaseProperties}, instead of specifying
   * bias in [-infty, infty], probability U == P(exposure=1|C=1) = {@link
   * exposureProbability4UnitContact#DiseaseProperties}, whose support is (0, 1), should be
   * provided. Then it is automatically converted into exp(bias) = U/(1-U) based on Sigmoid(bias) =
   * U.
   *
   * @param c
   * @param time
   * @return
   */
  Optional<InfectionEvent> evaluateExposures(ContactEvent c, int time) {
    Case personA = getMostSevere(population.get(c.to()), population.get(c.from()));
    Case personB =
        personA == population.get(c.to()) ? population.get(c.from()) : population.get(c.to());

    boolean dangerMix = personA.isInfectious() && personB.virusStatus() == SUSCEPTIBLE;

    double expBias =
        diseaseProperties.exposureProbability4UnitContact()
            / (1.0 - diseaseProperties.exposureProbability4UnitContact());
    double exposureProb =
        1. / (1. + 1. / (expBias * FastMath.pow(c.weight(), diseaseProperties.exposureExponent())));

    if (dangerMix && distributionSampler.uniformBetweenZeroAndOne() < exposureProb) {
      LOGGER.debug("       DANGER MIX");

      InfectionEvent infectionEvent =
          ImmutableInfectionEvent.builder()
              .id(personB.id())
              .time(c.time() + 1)
              .oldStatus(SUSCEPTIBLE)
              .nextStatus(EXPOSED)
              .exposedTime(time)
              .exposedBy(personA.id())
              .build();

      return Optional.of(infectionEvent);
    }
    return Optional.empty();
  }

  private Case getMostSevere(Case personA, Case personB) {
    int a = personA.virusStatus().getVal();
    int b = personB.virusStatus().getVal();

    return a > b ? personA : personB;
  }
}
