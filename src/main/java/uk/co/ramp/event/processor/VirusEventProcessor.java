package uk.co.ramp.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;

import java.util.Optional;
import java.util.stream.Collectors;

import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.AlertStatus.REQUESTED_TEST;
import static uk.co.ramp.people.VirusStatus.*;

@Service
public class VirusEventProcessor extends CommonVirusEventProcessor<VirusEvent> {
    private final AlertEventProcessor alertEventProcessor;
    private final Population population;

    @Autowired
    public VirusEventProcessor(Population population, DiseaseProperties diseaseProperties, DistributionSampler distributionSampler, AlertEventProcessor alertEventProcessor) {
        super(population, diseaseProperties, distributionSampler);
        this.population = population;
        this.alertEventProcessor = alertEventProcessor;
    }

    @Override
    public ProcessedEventResult processEvent(VirusEvent event) {
        Case thisCase = population.get(event.id());
        thisCase.setVirusStatus(thisCase.virusStatus().transitionTo(event.nextStatus()));

        VirusStatus nextStatus = determineNextStatus(event);

        // will return self if at DEAD or RECOVERED
        if (event.nextStatus() != nextStatus) {

            int deltaTime = timeInCompartment(event.nextStatus(), nextStatus);

            VirusEvent subsequentEvent = ImmutableVirusEvent.builder().
                    id(event.id()).
                    oldStatus(event.nextStatus()).
                    nextStatus(nextStatus).
                    time(event.time() + deltaTime).
                    eventProcessor(this).
                    build();

            Optional<AlertEvent> e = checkForAlert(subsequentEvent);

            return ImmutableProcessedEventResult.builder()
                    .addNewEvents(subsequentEvent)
                    .addAllNewEvents(e.stream().collect(Collectors.toList()))
                    .completedEvent(event)
                    .build();
        }
        return ImmutableProcessedEventResult.builder().build();
    }

    Optional<AlertEvent> checkForAlert(VirusEvent trigger) {

        if (trigger.nextStatus() == SYMPTOMATIC) {
            return Optional.of(ImmutableAlertEvent.builder().id(trigger.id()).time(trigger.time() + 1).oldStatus(NONE).nextStatus(REQUESTED_TEST).eventProcessor(alertEventProcessor).build());
        }

        return Optional.empty();
    }
}
