package uk.co.ramp.event;

import static uk.co.ramp.people.VirusStatus.*;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.BoundedDistribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.distribution.ImmutableBoundedDistribution;
import uk.co.ramp.event.types.CommonVirusEvent;
import uk.co.ramp.event.types.Event;
import uk.co.ramp.event.types.EventProcessor;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.VirusStatus;
import uk.ramp.distribution.Distribution;
import uk.ramp.distribution.ImmutableDistribution;

public abstract class CommonVirusEventProcessor<T extends Event> implements EventProcessor<T> {
  private static final Logger LOGGER = LogManager.getLogger(CommonVirusEventProcessor.class);

  private final Population population;
  private final StandardProperties properties;
  private final DiseaseProperties diseaseProperties;
  private final DistributionSampler distributionSampler;

  CommonVirusEventProcessor(
      Population population,
      StandardProperties properties,
      DiseaseProperties diseaseProperties,
      DistributionSampler distributionSampler) {
    this.population = population;
    this.diseaseProperties = diseaseProperties;
    this.distributionSampler = distributionSampler;
    this.properties = properties;
  }

  VirusStatus determineNextStatus(CommonVirusEvent event) {
    List<VirusStatus> newStatus = event.nextStatus().getValidTransitions();

    if (newStatus.isEmpty()) return event.nextStatus();
    if (newStatus.size() == 1) return newStatus.get(0);

    // these cases have multiple paths
    switch (event.nextStatus()) {
      case EXPOSED:
        return determineInfection(event);
      case SYMPTOMATIC:
        return determineSeverity(event);
      case SEVERELY_SYMPTOMATIC:
        return determineOutcome(event);
      default:
        LOGGER.error(event);
        throw new EventException("There is no case for the event" + event);
    }
  }

  int timeInCompartment(VirusStatus currentStatus, VirusStatus newStatus) {

    BoundedDistribution progressionData;
    switch (currentStatus) {
      case EXPOSED:
        progressionData = scaleWithTimeSteps(diseaseProperties.timeLatent());
        break;
      case PRESYMPTOMATIC:
        progressionData = scaleWithTimeSteps(diseaseProperties.timeSymptomsOnset());
        break;
      case ASYMPTOMATIC:
        progressionData = scaleWithTimeSteps(diseaseProperties.timeRecoveryAsymp());
        break;
      case SYMPTOMATIC:
        if (newStatus == SEVERELY_SYMPTOMATIC) {
          progressionData = scaleWithTimeSteps(diseaseProperties.timeDecline());
        } else {
          progressionData = scaleWithTimeSteps(diseaseProperties.timeRecoverySymp());
        }
        break;
      case SEVERELY_SYMPTOMATIC:
        if (newStatus == RECOVERED) {
          progressionData = scaleWithTimeSteps(diseaseProperties.timeRecoverySev());
        } else {
          progressionData = scaleWithTimeSteps(diseaseProperties.timeDeath());
        }
        break;
      default:
        String message = "Unexpected Virus statuses: " + currentStatus + " -> " + newStatus;
        LOGGER.error(message);
        throw new EventException(message);
    }

    return progressionData.getDistributionValue();
  }

  public BoundedDistribution scaleWithTimeSteps(BoundedDistribution distribution) {

    double scale =
        distribution.distribution().internalScale().orElse(distribution.max())
            * properties.timeStepsPerDay();

    Distribution internalDistribution =
        ImmutableDistribution.builder()
            .from(distribution.distribution())
            .internalScale(scale)
            .rng(distribution.distribution().rng())
            .internalType(distribution.distribution().internalType())
            .build();

    return ImmutableBoundedDistribution.builder()
        .from(distribution)
        .max(distribution.max() * properties.timeStepsPerDay())
        .distribution(internalDistribution)
        .build();
  }

  VirusStatus determineInfection(CommonVirusEvent e) {
    // TODO add real logic
    double health = population.getHealth(e.id());
    VirusStatus proposedVirusStatus =
        health > distributionSampler.uniformBetweenZeroAndOne() ? ASYMPTOMATIC : PRESYMPTOMATIC;
    return e.nextStatus().transitionTo(proposedVirusStatus);
  }

  VirusStatus determineSeverity(CommonVirusEvent e) {
    // TODO add real logic
    double health = population.getHealth(e.id());
    VirusStatus proposedVirusStatus =
        health > distributionSampler.uniformBetweenZeroAndOne() ? RECOVERED : SEVERELY_SYMPTOMATIC;
    return e.nextStatus().transitionTo(proposedVirusStatus);
  }

  VirusStatus determineOutcome(CommonVirusEvent e) {
    // TODO add real logic
    double health = population.getHealth(e.id());
    VirusStatus proposedVirusStatus =
        health > distributionSampler.uniformBetweenZeroAndOne() ? RECOVERED : DEAD;
    return e.nextStatus().transitionTo(proposedVirusStatus);
  }
}
