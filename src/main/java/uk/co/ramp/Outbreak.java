package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.InfectionMapException;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.PopulationGenerator;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.record.CmptRecord;
import uk.co.ramp.record.ImmutableCmptRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.VirusStatus.*;

@Service
public class Outbreak {

    private static final Logger LOGGER = LogManager.getLogger(Outbreak.class);
    public static final String INFECTION_MAP = "infectionMap.txt";

    private final StandardProperties properties;
    private final DiseaseProperties diseaseProperties;
    private final EventList eventList;
    private final InitialCaseReader initialCaseReader;
    private final EventProcessor eventProcessor;
    private final UtilitiesBean utils;
    private final LogDailyOutput outputLog;

    private Map<Integer, Case> population;

    private int activeCases = 0;

    private final Map<Integer, CmptRecord> records = new HashMap<>();

    public void setPopulation(Map<Integer, Case> population) {
        this.population = population;
    }

    public Outbreak(EventProcessor eventProcessor, EventList eventList, InitialCaseReader initialCaseReader, DiseaseProperties diseaseProperties, StandardProperties standardProperties, UtilitiesBean utils, LogDailyOutput outputLog) {

        this.eventProcessor = eventProcessor;
        this.eventList = eventList;
        this.initialCaseReader = initialCaseReader;
        this.diseaseProperties = diseaseProperties;
        this.properties = standardProperties;
        this.outputLog = outputLog;
        this.utils = utils;
    }

    @Autowired
    public void setIsolationPolicy(IsolationPolicy isolationPolicy) {
        this.isolationPolicy = isolationPolicy;
    }

    public Map<Integer, CmptRecord> propagate() {

        generateInitialInfection();
        LOGGER.info("Generated initial outbreak of {} cases", properties.initialExposures());
        runToCompletion();

        return records;
    }

    private void generateInitialInfection() {
        Set<Integer> infectedIds = infectPopulation();
        for (Integer id : infectedIds) {

        Set<Integer> infectedIds = initialCaseReader.getCases();
        List<Event> virusEvents = new ArrayList<>();

        ImmutableInfectionEvent genericEvent = ImmutableInfectionEvent.builder().
                exposedBy(Case.getInitial()).
                oldStatus(SUSCEPTIBLE).
                newStatus(EXPOSED).
                exposedTime(0).
                id(-1).
                time(0).build();

        infectedIds.forEach(id -> virusEvents.add(ImmutableInfectionEvent.builder().from(genericEvent).id(id).build()));
        eventList.addEvents(virusEvents);

    }


    void runToCompletion() {
        // the latest time to run to
        int timeLimit = properties.timeLimit();
        double randomInfectionRate = diseaseProperties.randomInfectionRate();

        runContactData(timeLimit, randomInfectionRate);

        new InfectionMap(population).outputMap();
        eventList.output();
    }


    void runContactData(int timeLimit, double randomInfectionRate) {
        int lastContact = eventList.getMap().keySet().stream().max(Comparator.naturalOrder()).orElseThrow();

        if (lastContact > timeLimit) {
            LOGGER.info("timeLimit it lower than time of last contact event");
            LOGGER.info("Not all contact data will be used");
        }

        for (int time = 0; time <= timeLimit; time++) {

            eventProcessor.setPopulation(population);
            eventProcessor.process(time, randomInfectionRate, lastContact);
            updateLogActiveCases(time);

            // stop random infections after contacts end
            if (activeCases == 0 && lastContact < time) {
                randomInfectionRate = 0d;
            }
        }

        tab--;
    }

    private double proportionOfPopulationInfectious(Map<Integer, Case> population) {
        return population.values().parallelStream().filter(Case::isInfectious).count() / (double) population.size();
    }

    private boolean runContactData(int maxContact, Map<Integer, Case> population, Map<Integer, List<ContactRecord>> contactRecords, double randomInfectionRate) {
        for (int time = 0; time <= maxContact; time++) {

            updatePopulationState(time, population, randomInfectionRate);
            List<ContactRecord> todaysContacts = contactRecords.get(time);
            int activeCases = logStepResults(population, time);

            if (activeCases == 0 && randomInfectionRate == 0d) {
                LOGGER.info("There are no active cases and the random infection rate is zero.");
                LOGGER.info("Exiting as solution is stable.");
                break;
            }

            double proportionInfectious = proportionOfPopulationInfectious(population);
            for (ContactRecord contacts : todaysContacts) {
                evaluateContact(population, time, contacts, proportionInfectious);
            }

        }
        return false;
    }

    private boolean isContactIsolated(ContactRecord contact, Case caseA, Case caseB, double proportionInfectious, int currentTime) {
       return isolationPolicy.isContactIsolated(caseA, caseB, contact.weight(), proportionInfectious, currentTime);
    }

    private void evaluateContact(Map<Integer, Case> population, int time, ContactRecord contacts, double proportionInfectious) {
        Case potentialSpreader = population.get(contacts.to());
        Case victim = population.get(contacts.from());
        boolean shouldIsolateContact = isContactIsolated(contacts, potentialSpreader, victim, proportionInfectious, time);
        if (shouldIsolateContact) {
            LOGGER.trace("Skipping contact due to isolation");
            return;
        }


        if (potentialSpreader.status() != victim.status()) {
            evaluateExposures(population, contacts, time);
        }
    }

        }
    }

    void updateLogActiveCases(int time) {
        int previousActiveCases = activeCases;
        Map<VirusStatus, Integer> stats = utils.getCmptCounts(population);
        activeCases = stats.get(EXPOSED) + stats.get(ASYMPTOMATIC) + stats.get(PRESYMPTOMATIC) + stats.get(SYMPTOMATIC) + stats.get(SEVERELY_SYMPTOMATIC);

        CmptRecord cmptRecord = outputLog.log(time, stats, activeCases - previousActiveCases);
        Set<Integer> alerts = new HashSet<>();

        for (Case p : population.values()) {
            EvaluateCase e = new EvaluateCase(p, diseaseProperties, distributionSampler);
            alerts.addAll(e.checkActionsAtTimestep(time));

            if (p.status() == SUSCEPTIBLE && randomInfectionRate > 0d && time > 0) {
                boolean var = distributionSampler.uniformBetweenZeroAndOne() <= randomInfectionRate;
                if (var) e.randomExposure(time);
            }
        }

        if (!alerts.isEmpty()) alertPopulation(alerts, population, time);
    }

    private void alertPopulation(Set<Integer> alerts, Map<Integer, Case> population, int time) {

        for (Integer id : alerts) {
            Case potentialInfected = population.get(id);
            if (potentialInfected.alertStatus() == NONE && potentialInfected.status() != DEAD) {
                potentialInfected.setNextAlertStatusChange(time + 1);
                LOGGER.trace("ALERTED: {} - {} - {} ", id, potentialInfected.status(), potentialInfected.alertStatus());
            }
        }

    }

    private Case getMostSevere(Case personA, Case personB) {
        int a = personA.status().getVal();
        int b = personB.status().getVal();

        return a > b ? personA : personB;
    }

    private void evaluateExposures(Map<Integer, Case> population, ContactRecord c, int time) {
        Case personA = getMostSevere(population.get(c.to()), population.get(c.from()));
        Case personB = personA == population.get(c.to()) ? population.get(c.from()) : population.get(c.to());

        personA.addContact(c);
        personB.addContact(c);

        boolean dangerMix = personA.isInfectious() && personB.status() == SUSCEPTIBLE;

        if (dangerMix && distributionSampler.uniformBetweenZeroAndOne() < c.weight() / diseaseProperties.exposureTuning()) {
            LOGGER.trace("       DANGER MIX");
            EvaluateCase e = new EvaluateCase(personB, diseaseProperties, distributionSampler);
            e.updateVirusStatus(EXPOSED, time, personA.id());
        }
    }


    private Set<Integer> infectPopulation() {

        Set<Integer> infectedIds = new HashSet<>();
        while (infectedIds.size() < properties.infected()) {
            infectedIds.add(distributionSampler.uniformInteger(properties.populationSize() - 1));
        }
        return infectedIds;

    }

    private int logStepResults(Map<Integer, Case> population, int time) {
        Map<VirusStatus, Integer> stats = PopulationGenerator.getCmptCounts(population);

        if (time == 0) {
            LOGGER.info("|   Time  |    S    |    E1   |    E2   |   Ia    |    Is   |    R    |    D    |");
        }

        CmptRecord cmptRecord = ImmutableCmptRecord.builder().time(time).
                s(stats.get(SUSCEPTIBLE)).
                e1(stats.get(EXPOSED)).
                e2(stats.get(EXPOSED_2)).
                ia(stats.get(INFECTED)).
                is(stats.get(INFECTED_SYMP)).
                r(stats.get(RECOVERED)).
                d(stats.get(DEAD)).build();


        int activeCases = stats.get(EXPOSED) + stats.get(EXPOSED_2) + stats.get(INFECTED) + stats.get(INFECTED_SYMP);

        String s = String.format("| %7d | %7d | %7d | %7d | %7d | %7d | %7d | %7d |",
                cmptRecord.time(),
                cmptRecord.s(),
                cmptRecord.e1(),
                cmptRecord.e2(),
                cmptRecord.ia(),
                cmptRecord.is(),
                cmptRecord.r(),
                cmptRecord.d());

        LOGGER.info(s);

        records.put(time, cmptRecord);
    }

}
