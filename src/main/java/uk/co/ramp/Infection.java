package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.people.Person;
import uk.co.ramp.people.PopulationGenerator;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.record.SeirRecord;

import java.util.*;

import static uk.co.ramp.people.VirusStatus.*;

public class Infection {

    private static final Logger LOGGER = LogManager.getLogger(Infection.class);

    private final StandardProperties properties;
    private final DiseaseProperties diseaseProperties;
    private final RandomDataGenerator rng;
    private final Map<Integer, Person> population;
    private final Map<Integer, List<ContactRecord>> contactRecords;
    private final Map<Integer, SeirRecord> records = new HashMap<>();

    public Infection(Map<Integer, Person> population, Map<Integer, List<ContactRecord>> contactRecords, StandardProperties runProperties, DiseaseProperties diseaseProperties, RandomDataGenerator rng) {
        this.properties = runProperties;
        this.diseaseProperties = diseaseProperties;
        this.rng = rng;
        this.population = population;
        this.contactRecords = contactRecords;
    }

    public Map<Integer, SeirRecord> propagate() {

        generateInitialInfection();
        runToCompletion();

        return records;
    }

    private void generateInitialInfection() {
        Set<Integer> infectedIds = infectPopulation();
        for (Integer id : infectedIds) {
            EvaluateCase evaluateCase = new EvaluateCase(population.get(id), diseaseProperties, rng);
            evaluateCase.updateStatus(EXPOSED, 0);
            LOGGER.info("population.get(id).getNextStatusChange() = {}", population.get(id).getNextStatusChange());
        }
    }

    private void runToCompletion() {
        int timeLimit = properties.getTimeLimit();
        int maxContact = contactRecords.keySet().stream().max(Comparator.naturalOrder()).orElseThrow(RuntimeException::new);
        int runTime;
        boolean steadyState = properties.isSteadyState();
        double randomInfectionRate = diseaseProperties.getRandomInfectionRate();

        if (timeLimit <= maxContact) {
            LOGGER.info("Not all contact data will be used");
            runTime = timeLimit;
            steadyState = false;
        } else {
            LOGGER.info("Potential for steady state soln");
            runTime = maxContact;

        }

        runContactData(runTime, population, contactRecords, randomInfectionRate);

        if (steadyState) {
            runToSteadyState(runTime, timeLimit, population);
        }


    }

    private void runContactData(int maxContact, Map<Integer, Person> population, Map<Integer, List<ContactRecord>> contactRecords, double randomInfectionRate) {
        for (int time = 0; time <= maxContact; time++) {

            updatePopulationState(time, population, randomInfectionRate);
            List<ContactRecord> todaysContacts = contactRecords.get(time);
            logStepResults(population, time);

            for (ContactRecord contacts : todaysContacts) {

                Person potentialSpreader = population.get(contacts.getTo());
                Person victim = population.get(contacts.getFrom());

                if (potentialSpreader.getStatus() != victim.getStatus()) {
                    evaluateExposures(population, contacts, time);
                }
            }
        }
    }


    private void runToSteadyState(int runTime, int timeLimit, Map<Integer, Person> population) {
        for (int time = runTime; time <= timeLimit; time++) {

            // set random infection rate to zero in steady state mode
            updatePopulationState(time, population, 0d);
            logStepResults(population, time);

            Map<VirusStatus, Integer> seirCounts = PopulationGenerator.getSEIRCounts(population);


            boolean exitCondition = seirCounts.get(EXPOSED) == 0 &&
                    seirCounts.get(EXPOSED_2) == 0 &&
                    seirCounts.get(INFECTED) == 0 &&
                    seirCounts.get(INFECTED_SYMP) == 0;


            if (exitCondition) {
                LOGGER.info("Steady state solution reached at t={} ", time);
                LOGGER.info("Exiting early.");
                return;
            }
        }
    }

    private void updatePopulationState(int time, Map<Integer, Person> population, double randomInfectionRate) {

        for (Person p : population.values()) {
            EvaluateCase e = new EvaluateCase(p, diseaseProperties, rng);
            e.checkTime(time);
            if (p.getStatus() == SUSCEPTIBLE && randomInfectionRate > 0d && time > 0) {

                boolean var = rng.nextUniform(0, 1) <= randomInfectionRate;
                if (var) e.randomExposure(time);
            }
        }
    }

    private Person getMostSevere(Person personA, Person personB) {
        VirusStatus a = personA.getStatus();
        VirusStatus b = personB.getStatus();

        return a.compareTo(b) > 0 ? personA : personB;
    }

    private void evaluateExposures(Map<Integer, Person> population, ContactRecord c, int time) {
        Person personA = getMostSevere(population.get(c.getTo()), population.get(c.getFrom()));
        Person personB = personA == population.get(c.getTo()) ? population.get(c.getFrom()) : population.get(c.getTo());

        boolean dangerMix = personA.isInfectious() && personB.getStatus() == SUSCEPTIBLE;

        if (dangerMix && rng.nextUniform(0, 1) < c.getWeight() / diseaseProperties.getExposureTuning()) {
            EvaluateCase e = new EvaluateCase(personB, diseaseProperties, rng);
            e.updateStatus(EXPOSED, time, personA.getId());
        }
    }


    private Set<Integer> infectPopulation() {

        Set<Integer> infectedIds = new HashSet<>();
        while (infectedIds.size() < properties.getInfected()) {
            infectedIds.add(rng.nextInt(0, properties.getPopulationSize()));
        }
        return infectedIds;

    }

    private void logStepResults(Map<Integer, Person> population, int time) {
        Map<VirusStatus, Integer> seirCounts = PopulationGenerator.getSEIRCounts(population);

        LOGGER.debug("Conditions @ time: {}", time);
        LOGGER.debug("{}    {}", SUSCEPTIBLE, seirCounts.get(SUSCEPTIBLE));
        LOGGER.debug("{}        {}", EXPOSED, seirCounts.get(EXPOSED));
        LOGGER.debug("{}      {}", EXPOSED_2, seirCounts.get(EXPOSED_2));
        LOGGER.debug("{}       {}", INFECTED, seirCounts.get(INFECTED));
        LOGGER.debug("{}  {}", INFECTED_SYMP, seirCounts.get(INFECTED_SYMP));
        LOGGER.debug("{}      {}", RECOVERED, seirCounts.get(RECOVERED));
        LOGGER.debug("{}           {}", DEAD, seirCounts.get(DEAD));

        SeirRecord seirRecord = new SeirRecord(time, seirCounts);

        records.put(time, seirRecord);

    }

}
