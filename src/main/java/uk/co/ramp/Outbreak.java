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

import static uk.co.ramp.people.AlertStatus.ALERTED;
import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.VirusStatus.*;

@Service
public class Outbreak {

    private static final Logger LOGGER = LogManager.getLogger(Outbreak.class);
    public static final String INFECTION_MAP = "infectionMap.txt";

    private StandardProperties properties;
    private DiseaseProperties diseaseProperties;
    private RandomDataGenerator rng;
    private int tab = 0;
    private Map<Integer, Case> population;
    private Map<Integer, List<ContactRecord>> contactRecords;
    private final Map<Integer, CmptRecord> records = new HashMap<>();

    public void setPopulation(Map<Integer, Case> population) {
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
            evaluateCase.updateVirusStatus(EXPOSED, 0);

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

        boolean complete = runContactData(runTime, population, contactRecords, randomInfectionRate);

        if (steadyState && !complete) {
            runToSteadyState(runTime, timeLimit, population);
        }

        outputInfectionData(population);


    }

    private void outputInfectionData(Map<Integer, Case> population) {
        List<Case> infections = new ArrayList<>();
        for (Case c : population.values()) {
            if (c.status() != SUSCEPTIBLE) {
                infections.add(c);
            }
        }

        infections.sort(Comparator.comparingInt(Case::exposedTime));

        Map<Integer, Set<Integer>> infectors = new HashMap<>();

        for (Case c : infections) {
            infectors.putIfAbsent(c.exposedBy(), new HashSet<>());

            Set<Integer> var = infectors.get(c.exposedBy());

            var.add(c.id());
            infectors.put(c.exposedBy(), var);
        }

        // initial infectees
        Set<Integer> infectees = infectors.get(Case.getDefault());

        // randomly infected
        infectees.addAll(infectors.getOrDefault(Case.getRandomInfection(), Collections.emptySet()));

        List<Integer> target = new ArrayList<>(infectees);

        try (Writer writer = new FileWriter(new File(INFECTION_MAP))) {
            recurseSet(target, infectors, writer);
        } catch (IOException e) {
            String message = "An error occurred while writing the map file: " + e.getMessage();
            LOGGER.error(message);
            throw new InfectionMapException(message);

        }

    }

    private void recurseSet(List<Integer> target, Map<Integer, Set<Integer>> infectors, Writer writer) throws IOException {

        tab++;
        for (int seed : target) {
            if (infectors.containsKey(seed)) {
                List<Integer> newSeeds = new ArrayList<>(infectors.get(seed));
                String spacer = "   ".repeat(tab - 1);
                if (tab == 1) {
                    writer.write("\n");
                    writer.write(seed + "  ->  " + newSeeds + "\n");
                } else {
                    writer.write(spacer + "   ->  " + seed + "   ->  " + newSeeds + "\n");
                }

                recurseSet(newSeeds, infectors, writer);
            }
        }

        tab--;
    }


    private boolean runContactData(int maxContact, Map<Integer, Case> population, Map<Integer, List<ContactRecord>> contactRecords, double randomInfectionRate) {
        for (int time = 0; time <= maxContact; time++) {

            updatePopulationState(time, population, randomInfectionRate);
            List<ContactRecord> todaysContacts = contactRecords.get(time);
            int activeCases = logStepResults(population, time);

            if (activeCases == 0 && randomInfectionRate == 0d) {
                LOGGER.info("There are no active cases and the random infection rate is zero.");
                LOGGER.info("Exiting as solution is stable.");
                return true;
            }

            for (ContactRecord contacts : todaysContacts) {
                evaluateContact(population, time, contacts);
            }

        }
        return false;
    }

    private void evaluateContact(Map<Integer, Case> population, int time, ContactRecord contacts) {
        Case potentialSpreader = population.get(contacts.to());
        Case victim = population.get(contacts.from());

        boolean conditionA = potentialSpreader.alertStatus() != NONE && victim.alertStatus() != NONE;
        boolean conditionB = contacts.weight() < diseaseProperties.exposureThreshold();


        if (conditionA && conditionB) {

            // TODO: Apply behavioural logic here. Use compliance value?
            LOGGER.trace("spreader: {}   victim: {}   weight: {} ", potentialSpreader.alertStatus(), victim.alertStatus(), contacts.weight());
            LOGGER.trace("Skipping contact due to threshold");
            return;
        }


        if (potentialSpreader.status() != victim.status()) {
            evaluateExposures(population, contacts, time);
        }
    }


    private void runToSteadyState(int runTime, int timeLimit, Map<Integer, Case> population) {
        for (int time = runTime; time <= timeLimit; time++) {

            // set random infection rate to zero in steady state mode
            updatePopulationState(time, population, 0d);
            logStepResults(population, time);

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

    private void updatePopulationState(int time, Map<Integer, Case> population, double randomInfectionRate) {

        for (Case p : population.values()) {
            EvaluateCase e = new EvaluateCase(p, diseaseProperties, rng);
            Set<Integer> alerts = e.checkActionsAtTimestep(time);

            if (alerts.isEmpty()) alertPopulation(alerts, population, time);

            if (p.status() == SUSCEPTIBLE && randomInfectionRate > 0d && time > 0) {
                boolean var = rng.nextUniform(0, 1) <= randomInfectionRate;
                if (var) e.randomExposure(time);
            }
        }
    }

    private void alertPopulation(Set<Integer> alerts, Map<Integer, Case> population, int time) {

        for (Integer id : alerts) {
            Case potentialInfected = population.get(id);
            if (potentialInfected.alertStatus() == NONE && potentialInfected.status() != DEAD) {
                potentialInfected.setNextAlertStatusChange(time + 1);
                potentialInfected.setAlertStatus(ALERTED);
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

        if (dangerMix && rng.nextUniform(0, 1) < c.weight() / diseaseProperties.exposureTuning()) {
            LOGGER.trace("       DANGER MIX");
            EvaluateCase e = new EvaluateCase(personB, diseaseProperties, rng);
            e.updateVirusStatus(EXPOSED, time, personA.id());
        }
    }


    private Set<Integer> infectPopulation() {

        Set<Integer> infectedIds = new HashSet<>();
        while (infectedIds.size() < properties.infected()) {
            infectedIds.add(rng.nextInt(0, properties.populationSize() - 1));
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

        return activeCases;


    }

}
