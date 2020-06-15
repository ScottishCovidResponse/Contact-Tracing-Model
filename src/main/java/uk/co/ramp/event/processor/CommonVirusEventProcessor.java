package uk.co.ramp.event.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.Distribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.distribution.ImmutableDistribution;
import uk.co.ramp.event.EventException;
import uk.co.ramp.event.types.CommonVirusEvent;
import uk.co.ramp.event.types.Event;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.utilities.MeanMax;

import java.util.List;

import static uk.co.ramp.people.VirusStatus.*;

public abstract class CommonVirusEventProcessor<T extends Event> implements EventProcessor<T> {
    private static final Logger LOGGER = LogManager.getLogger(CommonVirusEventProcessor.class);

    private final Population population;
    private final DiseaseProperties diseaseProperties;
    private final DistributionSampler distributionSampler;

    CommonVirusEventProcessor(Population population, DiseaseProperties diseaseProperties, DistributionSampler distributionSampler) {
        this.population = population;
        this.diseaseProperties = diseaseProperties;
        this.distributionSampler = distributionSampler;
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

        MeanMax progressionData;
        // TODO check when not exhausted
        switch (currentStatus) {
            case EXPOSED:
                progressionData = diseaseProperties.timeLatent();
                break;
            case PRESYMPTOMATIC:
                progressionData = diseaseProperties.timeSymptomsOnset();
                break;
            case ASYMPTOMATIC:
                progressionData = diseaseProperties.timeRecoveryAsymp();
                break;
            case SYMPTOMATIC:
                if (newStatus == SEVERELY_SYMPTOMATIC) {
                    progressionData = diseaseProperties.timeDecline();
                } else {
                    progressionData = diseaseProperties.timeRecoverySymp();
                }
                break;
            case SEVERELY_SYMPTOMATIC:
                if (newStatus == RECOVERED) {
                    progressionData = diseaseProperties.timeRecoverySev();
                } else {
                    progressionData = diseaseProperties.timeDeath();
                }
                break;
            default:
                String message = "Unexpected Virus statuses: " + currentStatus + " -> " + newStatus;
                LOGGER.error(message);
                throw new EventException(message);
        }

        Distribution distribution = ImmutableDistribution.builder().type(diseaseProperties.progressionDistribution()).mean(progressionData.mean()).max(progressionData.max()).build();
        return distributionSampler.getDistributionValue(distribution);
    }

    VirusStatus determineInfection(CommonVirusEvent e) {
        //TODO add real logic
        Case p = population.get(e.id());
        VirusStatus proposedVirusStatus = p.health() > distributionSampler.uniformBetweenZeroAndOne() ? ASYMPTOMATIC : PRESYMPTOMATIC;
        return e.nextStatus().transitionTo(proposedVirusStatus);
    }

    VirusStatus determineSeverity(CommonVirusEvent e) {
        //TODO add real logic
        Case p = population.get(e.id());
        VirusStatus proposedVirusStatus = p.health() > distributionSampler.uniformBetweenZeroAndOne() ? RECOVERED : SEVERELY_SYMPTOMATIC;
        return e.nextStatus().transitionTo(proposedVirusStatus);
    }

    VirusStatus determineOutcome(CommonVirusEvent e) {
        //TODO add real logic
        Case p = population.get(e.id());
        VirusStatus proposedVirusStatus = p.health() > distributionSampler.uniformBetweenZeroAndOne() ? RECOVERED : DEAD;
        return e.nextStatus().transitionTo(proposedVirusStatus);
    }
}
