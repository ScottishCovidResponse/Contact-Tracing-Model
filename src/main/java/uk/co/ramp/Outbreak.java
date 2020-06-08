package uk.co.ramp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.contact.ContactRecord;
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
    private final EventList eventList;
    private final InitialCaseReader initialCaseReader;
    private final EventProcessor eventProcessor;
    private final UtilitiesBean utils;
    private final LogDailyOutput outputLog;
    private final IsolationPolicy isolationPolicy;


    private final StandardProperties properties;
    private final DiseaseProperties diseaseProperties;
    private final DistributionSampler distributionSampler;
    private final UtilitiesBean utils;
    private final LogDailyOutput outputLog;
    private final InitialCaseReader initialCaseReader;


    private Map<Integer, Case> population;
    private Map<Integer, List<ContactRecord>> contactRecords;

    private final Map<Integer, CmptRecord> records = new HashMap<>();


    public Outbreak(DiseaseProperties diseaseProperties, StandardProperties standardProperties, DistributionSampler distributionSampler, UtilitiesBean utils, LogDailyOutput outputLog, InitialCaseReader initialCaseReader) {
        this.distributionSampler = distributionSampler;
        this.initialCaseReader = initialCaseReader;
        this.diseaseProperties = diseaseProperties;
        this.properties = standardProperties;
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
        LOGGER.info("Generated initial outbreak of {} cases", properties.initialExposures());
        runToCompletion();

        return records;
    }

    void generateInitialInfection() {

        Set<Integer> infectedIds = initialCaseReader.getCases();
        List<Event> virusEvents = new ArrayList<>();

        InfectionEvent genericEvent = ImmutableInfectionEvent.builder().
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

            if (activeCases == 0 && randomInfectionRate == 0d) {
                LOGGER.info("There are no active cases and the random infection rate is zero.");
                LOGGER.info("Exiting as solution is stable.");
                break;
            }
        }
    }

    void updateLogActiveCases(int time) {
        int previousActiveCases = activeCases;
        Map<VirusStatus, Integer> stats = utils.getCmptCounts(population);
        activeCases = stats.get(EXPOSED) + stats.get(ASYMPTOMATIC) + stats.get(PRESYMPTOMATIC) + stats.get(SYMPTOMATIC) + stats.get(SEVERELY_SYMPTOMATIC);

        CmptRecord cmptRecord = outputLog.log(time, stats, activeCases - previousActiveCases);
        records.put(time, cmptRecord);
    }

}
