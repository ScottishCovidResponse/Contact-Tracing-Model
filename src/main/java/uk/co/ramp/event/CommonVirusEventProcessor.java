package uk.co.ramp.event;

import static uk.co.ramp.people.VirusStatus.*;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.BoundedDistribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.CommonVirusEvent;
import uk.co.ramp.event.types.Event;
import uk.co.ramp.event.types.EventProcessor;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.VirusStatus;

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
        progressionData =
            EventProcessor.scaleWithTimeSteps(
                diseaseProperties.timeLatent(), properties.timeStepsPerDay());
        break;
      case PRESYMPTOMATIC:
        progressionData =
            EventProcessor.scaleWithTimeSteps(
                diseaseProperties.timeSymptomsOnset(), properties.timeStepsPerDay());
        break;
      case ASYMPTOMATIC:
        progressionData =
            EventProcessor.scaleWithTimeSteps(
                diseaseProperties.timeRecoveryAsymp(), properties.timeStepsPerDay());
        break;
      case SYMPTOMATIC:
        if (newStatus == SEVERELY_SYMPTOMATIC) {
          progressionData =
              EventProcessor.scaleWithTimeSteps(
                  diseaseProperties.timeDecline(), properties.timeStepsPerDay());
        } else {
          progressionData =
              EventProcessor.scaleWithTimeSteps(
                  diseaseProperties.timeRecoverySymp(), properties.timeStepsPerDay());
        }
        break;
      case SEVERELY_SYMPTOMATIC:
        if (newStatus == RECOVERED) {
          progressionData =
              EventProcessor.scaleWithTimeSteps(
                  diseaseProperties.timeRecoverySev(), properties.timeStepsPerDay());
        } else {
          progressionData =
              EventProcessor.scaleWithTimeSteps(
                  diseaseProperties.timeDeath(), properties.timeStepsPerDay());
        }
        break;
      default:
        String message = "Unexpected Virus statuses: " + currentStatus + " -> " + newStatus;
        LOGGER.error(message);
        throw new EventException(message);
    }

    return progressionData.getDistributionValue();
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
