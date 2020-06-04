package uk.co.ramp.event;


import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.utilities.MeanMax;
import uk.co.ramp.utilities.UtilitiesBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.co.ramp.people.AlertStatus.*;
import static uk.co.ramp.people.VirusStatus.*;

@Service
public class EventProcessor {

    private static final Logger LOGGER = LogManager.getLogger(EventProcessor.class);
    private DiseaseProperties diseaseProperties;
    private RandomDataGenerator rng;
    private Map<Integer, Case> population;
    private EventList eventList;
    private UtilitiesBean utils;

    @Autowired
    public void setUtilitiesBean(UtilitiesBean utils) {
        this.utils = utils;
    }

    @Autowired
    public void setEventList(EventList eventList) {
        this.eventList = eventList;
    }

    @Autowired
    public void setDiseaseProperties(DiseaseProperties diseaseProperties) {
        this.diseaseProperties = diseaseProperties;
    }

    @Autowired
    public void setRandomDataGenerator(RandomDataGenerator randomDataGenerator) {
        this.rng = randomDataGenerator;
    }


    public void setPopulation(Map<Integer, Case> population) {
        this.population = population;
    }


    public void process(int time, double randomInfectionRate, int randomCutoff) {

        List<Event> newEvents = new ArrayList<>();

        newEvents.addAll(runInfectionEvents(time));
        newEvents.addAll(runVirusEvents(time));
        newEvents.addAll(runContactEvents(time));
        newEvents.addAll(runAlertEvents(time));
        newEvents.addAll(runPolicyEvents(time));

        if (randomInfectionRate > 0d && time < randomCutoff) {
            newEvents.addAll(createRandomInfections(time, randomInfectionRate));
        }

        eventList.addEvents(newEvents);

    }

    List<InfectionEvent> createRandomInfections(int time, double randomRate) {

        List<Case> sus = population.values().stream().filter(aCase -> aCase.status() == SUSCEPTIBLE).collect(Collectors.toList());
        List<InfectionEvent> randomInfections = new ArrayList<>();
        for (Case aCase : sus) {
            if (rng.nextUniform(0, 1) < randomRate) {
                randomInfections.add(
                        ImmutableInfectionEvent.builder().
                                time(time + 1).id(aCase.id()).
                                newStatus(EXPOSED).
                                oldStatus(SUSCEPTIBLE).
                                exposedTime(time).
                                exposedBy(Case.getRandomInfection()).
                                build());
            }
        }

        return randomInfections;
    }

    List<Event> runPolicyEvents(int time) {
        //TODO
        return new ArrayList<>();
    }

    List<AlertEvent> runAlertEvents(int time) {
        List<AlertEvent> newEvents = new ArrayList<>();
        // Look at all alert events
        for (AlertEvent event : eventList.getForTime(time).stream().filter(event -> event instanceof AlertEvent).map(event -> (AlertEvent) event).collect(Collectors.toList())) {
            population.get(event.id()).processEvent(event, time);

            AlertStatus nextStatus = determineNextAlertStatus(event);

            if (nextStatus != event.newStatus()) {
                int nextTime = timeInStatus(nextStatus);

                AlertEvent subsequentEvent = ImmutableAlertEvent.builder().
                        id(event.id()).
                        oldStatus(event.newStatus()).
                        newStatus(nextStatus).
                        time(time + nextTime).
                        build();

                newEvents.add(subsequentEvent);
                eventList.completed(event);
            }
        }
        return newEvents;
    }

    List<InfectionEvent> runContactEvents(int time) {
        List<InfectionEvent> newEvents = new ArrayList<>();
        // Look at all contact events
        for (ContactEvent event : eventList.getForTime(time).stream().filter(event -> event instanceof ContactEvent).map(event -> (ContactEvent) event).collect(Collectors.toList())) {
            Optional<InfectionEvent> newEvent = evaluateContact(time, event);

            if (newEvent.isPresent()) {
                newEvents.add(newEvent.get());
                eventList.completed(event);
            }
        }
        return newEvents;

    }

    List<Event> runVirusEvents(int time) {
        List<Event> newEvents = new ArrayList<>();
        // Look at all virus events
        for (VirusEvent event : eventList.getForTime(time).stream().filter(event -> event instanceof VirusEvent).map(event -> (VirusEvent) event).collect(Collectors.toList())) {

            population.get(event.id()).processEvent(event, time);
            VirusStatus nextStatus = determineNextStatus(event);

            // will return self if at DEAD or RECOVERED
            if (event.newStatus() != nextStatus) {


                int nextTime = timeInCompartment(event.newStatus(), nextStatus);

                VirusEvent subsequentEvent = ImmutableVirusEvent.builder().
                        id(event.id()).
                        oldStatus(event.newStatus()).
                        newStatus(nextStatus).
                        time(time + nextTime).
                        build();

                Optional<Event> e = checkForAlert(subsequentEvent);

                newEvents.add(subsequentEvent);
                e.ifPresent(newEvents::add);
                eventList.completed(event);
            }
        }
        return newEvents;
    }

    List<VirusEvent> runInfectionEvents(int time) {
        List<VirusEvent> newEvents = new ArrayList<>();
        // Look at all infection events:
        for (InfectionEvent event : eventList.getForTime(time).stream().filter(event -> event instanceof InfectionEvent).map(event -> (InfectionEvent) event).collect(Collectors.toList())) {

            if (population.get(event.id()).status() == SUSCEPTIBLE) {
                population.get(event.id()).processEvent(event, time);

                VirusStatus nextStatus = determineNextStatus(event);
                int nextTime = timeInCompartment(event.newStatus(), nextStatus);

                VirusEvent subsequentEvent = ImmutableVirusEvent.builder().
                        id(event.id()).
                        oldStatus(event.newStatus()).
                        newStatus(nextStatus).
                        time(nextTime + time).
                        build();

                newEvents.add(subsequentEvent);
                eventList.completed(event);
            }
        }
        return newEvents;

    }


    Optional<InfectionEvent> evaluateContact(int time, ContactEvent contacts) {
        Case potentialSpreader = population.get(contacts.to());
        Case victim = population.get(contacts.from());

        boolean conditionA = potentialSpreader.alertStatus() != NONE || victim.alertStatus() != NONE;
        boolean conditionB = contacts.weight() < diseaseProperties.exposureThreshold();

        if (conditionA && conditionB) {
            // TODO: Apply behavioural logic here. Use compliance value?
            LOGGER.trace("spreader: {}   victim: {}   weight: {} ", potentialSpreader.alertStatus(), victim.alertStatus(), contacts.weight());
            LOGGER.debug("Skipping contact due to threshold");
            return Optional.empty();
        }


        if (potentialSpreader.status() != victim.status()) {
            return evaluateExposures(contacts, time);
        }

        return Optional.empty();
    }

    Optional<InfectionEvent> evaluateExposures(ContactEvent c, int time) {
        Case personA = utils.getMostSevere(population.get(c.to()), population.get(c.from()));
        Case personB = personA == population.get(c.to()) ? population.get(c.from()) : population.get(c.to());

        boolean dangerMix = personA.isInfectious() && personB.status() == SUSCEPTIBLE;

        if (dangerMix && rng.nextUniform(0, 1) < c.weight() / diseaseProperties.exposureTuning()) {
            LOGGER.debug("       DANGER MIX");

            InfectionEvent infectionEvent = ImmutableInfectionEvent.builder().
                    id(personB.id()).time(c.time() + 1).
                    oldStatus(SUSCEPTIBLE).newStatus(EXPOSED).
                    exposedTime(time).exposedBy(personA.id()).
                    build();


            return Optional.of(infectionEvent);

        }
        return Optional.empty();
    }

    private AlertStatus determineNextAlertStatus(AlertEvent event) {

        Case thisCase = population.get(event.id());

        List<AlertStatus> newStatus = getValidTransitions(event.newStatus());

        if (newStatus.isEmpty()) return event.newStatus();
        if (newStatus.size() == 1) return newStatus.get(0);

        switch (event.newStatus()) {
            case AWAITING_RESULT:
                return thisCase.isInfectious() ? TESTED_POSITIVE : TESTED_NEGATIVE;
            case NONE:
                return NONE;
            case TESTED_POSITIVE:
                return TESTED_POSITIVE;
        }

        System.out.println(event);

        throw new RuntimeException("should i be here?");

    }

    Optional<Event> checkForAlert(VirusEvent trigger) {

        if (trigger.newStatus() == SYMPTOMATIC) {
            return Optional.of(ImmutableAlertEvent.builder().id(trigger.id()).time(trigger.time() + 1).oldStatus(NONE).newStatus(REQUESTED_TEST).build());
        }

        return Optional.empty();
    }

    VirusStatus determineNextStatus(CommonVirusEvent event) {

        List<VirusStatus> newStatus = event.newStatus().getValidTransitions();

        if (newStatus.isEmpty()) return event.newStatus();
        if (newStatus.size() == 1) return newStatus.get(0);

        // these cases have multiple paths
        switch (event.newStatus()) {
            case EXPOSED:
                return determineInfection(population.get(event.id()));
            case SYMPTOMATIC:
                return determineSeverity(population.get(event.id()));
            case SEVERELY_SYMPTOMATIC:
                return determineOutcome(population.get(event.id()));
            default:
                System.out.println(event);
                throw new RuntimeException("Shouldn't get here");
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
                progressionData = diseaseProperties.timeRecoverySymp();
        }

        return getDistributionValue(progressionData);
    }

    int timeInStatus(AlertStatus newStatus) {
        // todo elaborate
        switch (newStatus) {
            case TESTED_POSITIVE:
                break;
            case TESTED_NEGATIVE:
                break;
            case AWAITING_RESULT:
                break;
            case REQUESTED_TEST:
                break;
            case ALERTED:
                break;
            case NONE:
                break;
        }
        MeanMax progressionData = diseaseProperties.timeTestAdministered();
        return getDistributionValue(progressionData);
    }

    int getDistributionValue(MeanMax progressionData) {
        double mean = progressionData.mean();
        double max = progressionData.max();

        int value = (int) Math.round(mean);
        double sample;
        switch (diseaseProperties.progressionDistribution()) {
            case GAUSSIAN:
                sample = rng.nextGaussian(mean, mean / 2d);
                break;
            case LINEAR:
                sample = rng.nextUniform(Math.max(mean - max, 1) / 2d, max);
                break;
            case EXPONENTIAL:
                sample = rng.nextExponential(mean);
                break;
            case FLAT:
            default:
                return value;
        }

        value = (int) Math.round(sample);

        return Math.min(Math.max(value, 1), (int) max);
    }


    VirusStatus determineInfection(Case p) {
        //TODO add real logic
        return p.health() > rng.nextUniform(0, 1) ? ASYMPTOMATIC : PRESYMPTOMATIC;
    }

    VirusStatus determineSeverity(Case p) {
        //TODO add real logic
        return p.health() > rng.nextUniform(0, 1) ? RECOVERED : SEVERELY_SYMPTOMATIC;
    }

    VirusStatus determineOutcome(Case p) {
        //TODO add real logic
        return p.health() > rng.nextUniform(0, 1) ? RECOVERED : DEAD;
    }
}
