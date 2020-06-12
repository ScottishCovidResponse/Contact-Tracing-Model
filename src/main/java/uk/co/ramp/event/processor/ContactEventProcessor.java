package uk.co.ramp.event.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.policy.IsolationPolicy;
import uk.co.ramp.utilities.UtilitiesBean;

import java.util.Optional;

import static uk.co.ramp.people.VirusStatus.EXPOSED;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

@Service
public class ContactEventProcessor implements EventProcessor<ContactEvent> {
    private static final Logger LOGGER = LogManager.getLogger(ContactEventProcessor.class);

    private final Population population;
    private final DiseaseProperties diseaseProperties;
    private final DistributionSampler distributionSampler;
    private final IsolationPolicy isolationPolicy;
    private final UtilitiesBean utils;
    private final InfectionEventProcessor infectionEventProcessor;

    @Autowired
    public ContactEventProcessor(Population population, DiseaseProperties diseaseProperties, DistributionSampler distributionSampler, IsolationPolicy isolationPolicy, UtilitiesBean utils, InfectionEventProcessor infectionEventProcessor) {
        this.population = population;
        this.diseaseProperties = diseaseProperties;
        this.distributionSampler = distributionSampler;
        this.isolationPolicy = isolationPolicy;
        this.utils = utils;
        this.infectionEventProcessor = infectionEventProcessor;
    }

    @Override
    public ProcessedEventResult processEvent(ContactEvent event) {
        Optional<InfectionEvent> newEvent = evaluateContact(event, proportionOfPopulationInfectious());
        return newEvent.map(e -> ImmutableProcessedEventResult.builder()
                .addNewEvents(e)
                .completedEvent(event)
                .build())
                .orElseGet(() -> ImmutableProcessedEventResult.builder().build());
    }

    private double proportionOfPopulationInfectious() {
        return population.proportionInfectious();
    }

    private boolean isContactIsolated(ContactEvent contact, Case caseA, Case caseB, double proportionInfectious, int currentTime) {
        return isolationPolicy.isContactIsolated(caseA, caseB, contact.weight(), proportionInfectious, currentTime);
    }

    Optional<InfectionEvent> evaluateContact(ContactEvent contacts, double proportionInfectious) {
        int time = contacts.time();
        Case potentialSpreader = population.get(contacts.to());
        Case victim = population.get(contacts.from());

        boolean shouldIsolateContact = isContactIsolated(contacts, potentialSpreader, victim, proportionInfectious, time);

        if (shouldIsolateContact) {
            LOGGER.trace("Skipping contact due to isolation");
            return Optional.empty();
        }

        if (potentialSpreader.virusStatus() != victim.virusStatus()) {
            return evaluateExposures(contacts, time);
        }

        return Optional.empty();
    }

    Optional<InfectionEvent> evaluateExposures(ContactEvent c, int time) {
        Case personA = utils.getMostSevere(population.get(c.to()), population.get(c.from()));
        Case personB = personA == population.get(c.to()) ? population.get(c.from()) : population.get(c.to());

        boolean dangerMix = personA.isInfectious() && personB.virusStatus() == SUSCEPTIBLE;

        if (dangerMix && distributionSampler.uniformBetweenZeroAndOne() < c.weight() / diseaseProperties.exposureTuning()) {
            LOGGER.debug("       DANGER MIX");

            InfectionEvent infectionEvent = ImmutableInfectionEvent.builder().
                    id(personB.id()).time(c.time() + 1).
                    oldStatus(SUSCEPTIBLE).nextStatus(EXPOSED).
                    exposedTime(time).exposedBy(personA.id()).
                    eventProcessor(infectionEventProcessor).
                    build();


            return Optional.of(infectionEvent);

        }
        return Optional.empty();
    }
}
