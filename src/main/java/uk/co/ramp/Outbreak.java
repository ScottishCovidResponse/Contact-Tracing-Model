package uk.co.ramp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.InfectionMap;
import uk.co.ramp.io.LogDailyOutput;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.PopulationGenerator;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.policy.IsolationPolicy;
import uk.co.ramp.record.CmptRecord;
import uk.co.ramp.utilities.UtilitiesBean;

import java.util.*;

import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.VirusStatus.*;

@Service
public class Outbreak {

    private static final Logger LOGGER = LogManager.getLogger(Outbreak.class);

    private final StandardProperties properties;
    private final DiseaseProperties diseaseProperties;
    private final RandomDataGenerator rng;
    private final UtilitiesBean utils;
    private final LogDailyOutput outputLog;


    private Map<Integer, Case> population;
    private Map<Integer, List<ContactRecord>> contactRecords;

    private final Map<Integer, CmptRecord> records = new HashMap<>();

    @Autowired
    public Outbreak(DiseaseProperties diseaseProperties, StandardProperties standardProperties, RandomDataGenerator randomDataGenerator, UtilitiesBean utils, LogDailyOutput outputLog) {

        this.diseaseProperties = diseaseProperties;
        this.properties = standardProperties;
        this.rng = randomDataGenerator;
        this.utils = utils;
        this.outputLog = outputLog;

    }

    public void setPopulation(Map<Integer, Case> population) {
        this.population = population;
    }

    public void setContactRecords(Map<Integer, List<ContactRecord>> contactRecords) {
        this.contactRecords = contactRecords;
    }

    public Map<Integer, CmptRecord> propagate() {

        generateInitialInfection();
        LOGGER.info("Generated initial outbreak of {} cases", properties.infected());
        runToCompletion();

        return records;
    }

    void generateInitialInfection() {
        Set<Integer> infectedIds = chooseInitialInfected();
        for (Integer id : infectedIds) {

            EvaluateCase evaluateCase = new EvaluateCase(population.get(id), diseaseProperties, distributionSampler);
            evaluateCase.updateVirusStatus(EXPOSED, 0, Case.getInitial());

        }
    }


    void runToCompletion() {
        int timeLimit = properties.timeLimit();
        int maxContact = contactRecords.keySet().stream().max(Comparator.naturalOrder()).orElseThrow(RuntimeException::new);
        int runTime;
        boolean steadyState = properties.steadyState();
        double randomInfectionRate = diseaseProperties.randomInfectionRate();

        if (timeLimit <= maxContact) {
            LOGGER.warn("Not all contact data will be used");
            runTime = timeLimit;
            steadyState = false;
        } else {
            LOGGER.info("Potential for steady state soln");
            runTime = maxContact;
        }

        boolean complete = runContactData(runTime, randomInfectionRate);

        if (steadyState && !complete) {
            runToSteadyState(runTime, timeLimit);
        }

        // Output map of infections
        new InfectionMap(Collections.unmodifiableMap(population)).outputMap();

    }


    boolean runContactData(int maxContact, double randomInfectionRate) {

        for (int time = 0; time <= maxContact; time++) {

            updatePopulationState(time, randomInfectionRate);
            List<ContactRecord> todaysContacts = contactRecords.get(time);
            int activeCases = calculateDailyStatistics(time);

            if (activeCases == 0 && randomInfectionRate == 0d) {
                LOGGER.info("There are no active cases and the random infection rate is zero.");
                LOGGER.info("Exiting as solution is stable.");
                return true;
            }

            double proportionInfectious = proportionOfPopulationInfectious(population);
            for (ContactRecord contacts : todaysContacts) {
                evaluateContact(time, contacts);
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
            evaluateExposures(contacts, time);
        }
    }


    void runToSteadyState(int runTime, int timeLimit) {
        for (int time = runTime; time <= timeLimit; time++) {

            // set random infection rate to zero in steady state mode
            updatePopulationState(time, 0d);
            calculateDailyStatistics(time);

            Map<VirusStatus, Integer> compartmentCounts = PopulationGenerator.getCmptCounts(population);


            boolean exitCondition = compartmentCounts.get(EXPOSED) == 0 &&
                    compartmentCounts.get(EXPOSED_2) == 0 &&
                    compartmentCounts.get(INFECTED) == 0 &&
                    compartmentCounts.get(INFECTED_SYMP) == 0;


            if (exitCondition) {
                LOGGER.info("Steady state solution reached at t={} ", time);
                LOGGER.info("Exiting early.");
                return;
            }
        }
    }

    void updatePopulationState(int time, double randomInfectionRate) {

        Set<Integer> alerts = new HashSet<>();

        for (Case p : population.values()) {
            EvaluateCase e = new EvaluateCase(p, diseaseProperties, distributionSampler);
            alerts.addAll(e.checkActionsAtTimestep(time));

            if (p.status() == SUSCEPTIBLE && randomInfectionRate > 0d && time > 0) {
                boolean var = distributionSampler.uniformBetweenZeroAndOne() <= randomInfectionRate;
                if (var) e.randomExposure(time);
            }
        }

        if (!alerts.isEmpty()) alertPopulation(alerts, time);
    }

    void alertPopulation(Set<Integer> alerts, int time) {

        for (Integer id : alerts) {
            Case potentialInfected = population.get(id);
            if (potentialInfected.alertStatus() == NONE && potentialInfected.status() != DEAD) {
                potentialInfected.setNextAlertStatusChange(time + 1);
                LOGGER.trace("ALERTED: {} - {} - {} ", id, potentialInfected.status(), potentialInfected.alertStatus());
            }
        }

    }


    void evaluateExposures(ContactRecord c, int time) {
        Case personA = utils.getMostSevere(population.get(c.to()), population.get(c.from()));
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


    Set<Integer> chooseInitialInfected() {

        Set<Integer> infectedIds = new HashSet<>();
        while (infectedIds.size() < properties.infected()) {
            infectedIds.add(distributionSampler.uniformInteger(properties.populationSize() - 1));
        }
        return infectedIds;

    }

    int calculateDailyStatistics(int time) {
        Map<VirusStatus, Integer> stats = utils.getCmptCounts(population);
        int activeCases = stats.get(EXPOSED) + stats.get(EXPOSED_2) + stats.get(INFECTED) + stats.get(INFECTED_SYMP);

        CmptRecord cmptRecord = outputLog.log(time, stats);
        records.put(time, cmptRecord);

        return activeCases;

    }

}
