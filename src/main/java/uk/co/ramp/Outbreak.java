package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.event.ContactEvent;
import uk.co.ramp.event.Event;
import uk.co.ramp.event.EventList;
import uk.co.ramp.io.InfectionMap;
import uk.co.ramp.io.InitialCaseReader;
import uk.co.ramp.io.LogDailyOutput;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.PopulationGenerator;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.utilities.UtilitiesBean;

import java.util.*;

import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.VirusStatus.*;

@Service
public class Outbreak {

    private static final Logger LOGGER = LogManager.getLogger(Outbreak.class);

    private StandardProperties properties;
    private DiseaseProperties diseaseProperties;
    private RandomDataGenerator rng;
    private InitialCaseReader initialCaseReader;
    private Map<Integer, Case> population;
    //    private Map<Integer, List<ContactRecord>> contactRecords;
    private EventList eventList;

    private final LogDailyOutput outputLog = new LogDailyOutput();


    private final Map<Integer, CmptRecord> records = new HashMap<>();
    private UtilitiesBean utils;


    public void setPopulation(Map<Integer, Case> population) {
        this.population = population;
    }

//    public void setContactRecords(Map<Integer, List<ContactRecord>> contactRecords) {
//        this.contactRecords = contactRecords;
//    }

    @Autowired
    public void setEventList(EventList eventList) {
        this.eventList = eventList;
    }

    @Autowired
    public void setInitialCaseReader(InitialCaseReader initialCaseReader) {
        this.initialCaseReader = initialCaseReader;
    }

    @Autowired
    public void setDiseaseProperties(DiseaseProperties diseaseProperties) {
        this.diseaseProperties = diseaseProperties;
    }

    @Autowired
    public void setStandardProperties(StandardProperties standardProperties) {
        this.properties = standardProperties;
    }

    @Autowired
    public void setRandomDataGenerator(RandomDataGenerator randomDataGenerator) {
        this.rng = randomDataGenerator;
    }

    @Autowired
    public void setUtilitiesBean(UtilitiesBean utils) {
        this.utils = utils;
    }


    public Map<Integer, CmptRecord> propagate() {

        generateInitialInfection();
        LOGGER.info("Generated initial outbreak of {} cases", properties.initialExposures());
        runToCompletion();

        return records;
    }

    void generateInitialInfection() {

        Set<Integer> infectedIds = initialCaseReader.getCases();

        for (Integer id : infectedIds) {

            EvaluateCase evaluateCase = new EvaluateCase(population.get(id), diseaseProperties, rng);
            evaluateCase.updateVirusStatus(EXPOSED, 0, Case.getInitial());

        }
    }


    void runToCompletion() {
        int timeLimit = properties.timeLimit();
        int maxContact = eventList.getMap().keySet().stream().max(Comparator.naturalOrder()).orElseThrow();

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
        new InfectionMap(population).outputMap();

    }


    boolean runContactData(int maxContact, double randomInfectionRate) {
        for (int time = 0; time <= maxContact; time++) {

            updatePopulationState(time, randomInfectionRate);
            List<Event> todaysContacts = eventList.getForTime(time);
            int activeCases = calculateDailyStatistics(time);

            if (activeCases == 0 && randomInfectionRate == 0d) {
                LOGGER.info("There are no active cases and the random infection rate is zero.");
                LOGGER.info("Exiting as solution is stable.");
                return true;
            }

            for (Event contacts : todaysContacts) {
                if (contacts instanceof ContactEvent) evaluateContact(time, (ContactEvent) contacts);
            }

        }
        return false;
    }

    void evaluateContact(int time, ContactEvent contacts) {
        Case potentialSpreader = population.get(contacts.to());
        Case victim = population.get(contacts.from());

        boolean conditionA = potentialSpreader.alertStatus() != NONE || victim.alertStatus() != NONE;
        boolean conditionB = contacts.weight() < diseaseProperties.exposureThreshold();


        if (conditionA && conditionB) {

            // TODO: Apply behavioural logic here. Use compliance value?
            LOGGER.trace("spreader: {}   victim: {}   weight: {} ", potentialSpreader.alertStatus(), victim.alertStatus(), contacts.weight());
            LOGGER.debug("Skipping contact due to threshold");
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
            EvaluateCase e = new EvaluateCase(p, diseaseProperties, rng);
            alerts.addAll(e.checkActionsAtTimestep(time));

            if (p.status() == SUSCEPTIBLE && randomInfectionRate > 0d && time > 0) {
                boolean var = rng.nextUniform(0, 1) <= randomInfectionRate;
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


    void evaluateExposures(ContactEvent c, int time) {
        Case personA = utils.getMostSevere(population.get(c.to()), population.get(c.from()));
        Case personB = personA == population.get(c.to()) ? population.get(c.from()) : population.get(c.to());

        personA.addContact(c);
        personB.addContact(c);

        boolean dangerMix = personA.isInfectious() && personB.status() == SUSCEPTIBLE;

        if (dangerMix && rng.nextUniform(0, 1) < c.weight() / diseaseProperties.exposureTuning()) {
            LOGGER.trace("       DANGER MIX");
            EvaluateCase e = new EvaluateCase(personB, diseaseProperties, rng);
            e.updateVirusStatus(EXPOSED, time, personA.id());
        }
    }

    int calculateDailyStatistics(int time) {
        Map<VirusStatus, Integer> stats = utils.getCmptCounts(population);
        int activeCases = stats.get(EXPOSED) + stats.get(EXPOSED_2) + stats.get(INFECTED) + stats.get(INFECTED_SYMP);

        CmptRecord cmptRecord = outputLog.log(time, stats);
        records.put(time, cmptRecord);

        return activeCases;

    }

}
