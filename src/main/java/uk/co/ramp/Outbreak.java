package uk.co.ramp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.event.EventList;
import uk.co.ramp.event.EventProcessor;
import uk.co.ramp.event.types.Event;
import uk.co.ramp.event.types.ImmutableInfectionEvent;
import uk.co.ramp.event.types.InfectionEvent;
import uk.co.ramp.io.InfectionMap;
import uk.co.ramp.io.InitialCaseReader;
import uk.co.ramp.io.LogDailyOutput;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.utilities.UtilitiesBean;

import java.util.*;

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

    private int activeCases;

    private Map<Integer, Case> population;
    private final Map<Integer, CmptRecord> records = new HashMap<>();

    @Autowired
    public Outbreak(DiseaseProperties diseaseProperties, StandardProperties standardProperties,
                    UtilitiesBean utils, LogDailyOutput outputLog, InitialCaseReader initialCaseReader,
                    EventList eventList, EventProcessor eventProcessor) {

        this.initialCaseReader = initialCaseReader;
        this.diseaseProperties = diseaseProperties;
        this.properties = standardProperties;
        this.utils = utils;
        this.outputLog = outputLog;
        this.eventProcessor = eventProcessor;
        this.eventList = eventList;

    }

    public void setPopulation(Map<Integer, Case> population) {
        this.population = population;
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

        infectedIds.forEach(id -> virusEvents.add(ImmutableInfectionEvent.copyOf(genericEvent).withId(id)));
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
