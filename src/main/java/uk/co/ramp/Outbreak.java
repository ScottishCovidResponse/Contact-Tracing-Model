package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.people.Person;
import uk.co.ramp.people.PopulationGenerator;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.record.CmptRecord;
import uk.co.ramp.record.ImmutableCmptRecord;

import java.util.*;

import static uk.co.ramp.people.VirusStatus.*;

@Service
public class Outbreak {

    private static final Logger LOGGER = LogManager.getLogger(Outbreak.class);

    private StandardProperties properties;
    private DiseaseProperties diseaseProperties;
    private RandomDataGenerator rng;


    private Map<Integer, Person> population;
    private Map<Integer, List<ContactRecord>> contactRecords;
    private final Map<Integer, CmptRecord> records = new HashMap<>();

    public void setPopulation(Map<Integer, Person> population) {
        this.population = population;
    }

    public void setContactRecords(Map<Integer, List<ContactRecord>> contactRecords) {
        this.contactRecords = contactRecords;
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


    public Map<Integer, CmptRecord> propagate() {

        generateInitialInfection();
        runToCompletion();

        return records;
    }

    private void generateInitialInfection() {
        Set<Integer> infectedIds = infectPopulation();
        for (Integer id : infectedIds) {

            EvaluateCase evaluateCase = new EvaluateCase(population.get(id), diseaseProperties, rng);
            evaluateCase.updateStatus(EXPOSED, 0);

        }
    }

    private void runToCompletion() {
        int timeLimit = properties.timeLimit();
        int maxContact = contactRecords.keySet().stream().max(Comparator.naturalOrder()).orElseThrow(RuntimeException::new);
        int runTime;
        boolean steadyState = properties.steadyState();
        double randomInfectionRate = diseaseProperties.randomInfectionRate();

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

                Person potentialSpreader = population.get(contacts.to());
                Person victim = population.get(contacts.from());

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
        Person personA = getMostSevere(population.get(c.to()), population.get(c.from()));
        Person personB = personA == population.get(c.to()) ? population.get(c.from()) : population.get(c.to());

        boolean dangerMix = personA.isInfectious() && personB.getStatus() == SUSCEPTIBLE;

        if (dangerMix && rng.nextUniform(0, 1) < c.weight() / diseaseProperties.exposureTuning()) {
            EvaluateCase e = new EvaluateCase(personB, diseaseProperties, rng);
            e.updateStatus(EXPOSED, time, personA.getId());
        }
    }


    private Set<Integer> infectPopulation() {

        Set<Integer> infectedIds = new HashSet<>();
        while (infectedIds.size() < properties.infected()) {
            infectedIds.add(rng.nextInt(0, properties.populationSize()));
        }
        return infectedIds;

    }

    private void logStepResults(Map<Integer, Person> population, int time) {
        Map<VirusStatus, Integer> stats = PopulationGenerator.getSEIRCounts(population);

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
