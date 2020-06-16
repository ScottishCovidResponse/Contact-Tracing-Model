package uk.co.ramp.event.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.Distribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.distribution.ImmutableDistribution;
import uk.co.ramp.event.EventException;
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.ImmutableAlertEvent;
import uk.co.ramp.event.types.ImmutableProcessedEventResult;
import uk.co.ramp.event.types.ProcessedEventResult;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.Case;
import uk.co.ramp.utilities.ImmutableMeanMax;
import uk.co.ramp.utilities.MeanMax;

import java.util.List;

import static uk.co.ramp.people.AlertStatus.*;
import static uk.co.ramp.people.AlertStatus.NONE;

@Service
public class AlertEventProcessor implements EventProcessor<AlertEvent> {
    private static final Logger LOGGER = LogManager.getLogger(AlertEventProcessor.class);

    private final Population population;
    private final DiseaseProperties diseaseProperties;
    private final DistributionSampler distributionSampler;

    @Autowired
    public AlertEventProcessor(Population population, DiseaseProperties diseaseProperties, DistributionSampler distributionSampler) {
        this.population = population;
        this.diseaseProperties = diseaseProperties;
        this.distributionSampler = distributionSampler;
    }

    @Override
    public ProcessedEventResult processEvent(AlertEvent event) {
        Case thisCase = population.get(event.id());
        thisCase.setAlertStatus(event.nextStatus());

        AlertStatus proposedStatus = determineNextAlertStatus(event);
        AlertStatus nextStatus = event.nextStatus().transitionTo(proposedStatus);

        if (nextStatus != event.nextStatus()) {
            int deltaTime = timeInStatus(nextStatus);

            AlertEvent subsequentEvent = ImmutableAlertEvent.builder().
                    id(event.id()).
                    oldStatus(event.nextStatus()).
                    nextStatus(nextStatus).
                    time(event.time() + deltaTime).
                    eventProcessor(this).
                    build();

            return ImmutableProcessedEventResult.builder()
                    .addNewEvents(subsequentEvent)
                    .completedEvent(event)
                    .build();
        }
        return ImmutableProcessedEventResult.builder().build();
    }

    private AlertStatus determineNextAlertStatus(AlertEvent event) {
        Case thisCase = population.get(event.id());
        List<AlertStatus> newStatus = getValidTransitions(event.nextStatus());

        if (newStatus.isEmpty()) return event.nextStatus();
        if (newStatus.size() == 1) return newStatus.get(0);

        switch (event.nextStatus()) {
            case AWAITING_RESULT:
                return thisCase.isInfectious() ? TESTED_POSITIVE : TESTED_NEGATIVE;
            case NONE:
                return NONE;
            default:
                LOGGER.error(event);
                throw new EventException("There is no case for the event" + event);
        }
    }

    int timeInStatus(AlertStatus newStatus) {

        MeanMax progressionData;
        switch (newStatus) {
            case TESTED_NEGATIVE:
                progressionData = ImmutableMeanMax.builder().mean(1).max(1).build();
                break;
            case AWAITING_RESULT:
                progressionData = diseaseProperties.timeTestResult();
                break;
            case REQUESTED_TEST:
                progressionData = diseaseProperties.timeTestAdministered();
                break;
            case ALERTED:
                progressionData = ImmutableMeanMax.builder().mean(1).max(1).build();
                break;
            default:
                return 0;
        }

        Distribution distribution = ImmutableDistribution.builder().type(diseaseProperties.progressionDistribution()).mean(progressionData.mean()).max(progressionData.max()).build();
        return distributionSampler.getDistributionValue(distribution);
    }
}
