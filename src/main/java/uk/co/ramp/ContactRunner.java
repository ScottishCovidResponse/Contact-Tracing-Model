package uk.co.ramp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.Output;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.io.readers.DiseasePropertiesReader;
import uk.co.ramp.io.readers.PopulationPropertiesReader;
import uk.co.ramp.io.readers.StandardPropertiesReader;
import uk.co.ramp.people.Person;
import uk.co.ramp.people.PopulationGenerator;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.record.SeirRecord;
import uk.co.ramp.utilities.ContactReader;
import uk.co.ramp.utilities.RandomSingleton;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static uk.co.ramp.people.VirusStatus.*;

public class ContactRunner {

    private static final Logger LOGGER = LogManager.getLogger(ContactRunner.class);
    private static Random rng;
    private final Map<Integer, SeirRecord> records = new HashMap<>();

    private static PopulationProperties populationProperties;
    private static StandardProperties runProperties;
    private static DiseaseProperties diseaseProperties;


    public static void main(String[] args) throws IOException {
        ContactRunner contactRunner = new ContactRunner();

        readInputs();

        rng = RandomSingleton.getInstance(runProperties.getSeed());
        Optional<Integer> sid = Optional.empty();
        if (args.length == 1) {
            sid = Optional.of(Integer.parseInt(args[0]));
        }
        contactRunner.run(sid);
    }

    private static void readInputs() throws IOException {
        runProperties = StandardPropertiesReader.read(new File("input/runSettings.json"));
        populationProperties = PopulationPropertiesReader.read(new File("input/populationSettings.json"));
        diseaseProperties = DiseasePropertiesReader.read(new File("input/diseaseSettings.json"));
    }

    public static DiseaseProperties getDiseaseProperties() {
        return diseaseProperties;
    }

    public static StandardProperties getRunProperties() {
        return runProperties;
    }

    private void runToSteadyState(int runTime, int timeLimit, Map<Integer, Person> population) {
        for (int time = runTime; time <= timeLimit; time++) {

            // set random infection rate to zero in steady state mode
            updatePopulationState(time, population, 0d);
            printSEIR(population, time);

            Map<VirusStatus, Integer> seirCounts = PopulationGenerator.getSEIRCounts(population);

            if (seirCounts.get(EXPOSED) == 0 && seirCounts.get(INFECTED) == 0) {
                LOGGER.info("Steady state solution reached at t={} ", time);
                LOGGER.info("Exiting early.");
                return;
            }
        }
    }


    private void runContactData(int maxContact, Map<Integer, Person> population, Map<Integer, List<ContactRecord>> contactRecords, double randomInfectionRate) {
        for (int time = 0; time <= maxContact; time++) {

            updatePopulationState(time, population, randomInfectionRate);
            List<ContactRecord> todaysContacts = contactRecords.get(time);
            printSEIR(population, time);

            for (ContactRecord contacts : todaysContacts) {

                Person potentialSpreader = population.get(contacts.getTo());
                Person victim = population.get(contacts.getFrom());

                if (potentialSpreader.getStatus() != victim.getStatus()) {
                    evaluateExposures(population, contacts, time);
                }
            }
        }
    }

    public static Random getRng() {
        return rng;
    }

    private void run(Optional<Integer> sidCmd) throws IOException {

        // readInputs();

        if (sidCmd.isPresent()) {
            int seed = runProperties.getSeed();
            LOGGER.warn("The seed from input file will be modified from {} to {}", sidCmd.get(), seed + sidCmd.get());
            seed += sidCmd.get();
            runProperties.setSeed(seed);
        }


        Map<Integer, Person> population = PopulationGenerator.generate(runProperties, populationProperties);
        Map<Integer, List<ContactRecord>> contactRecords = ContactReader.read(runProperties);
        Set<Integer> infectedIds = infectPopulation();

        LOGGER.info(infectedIds);

        for (Integer id : infectedIds) {
            population.get(id).updateStatus(INFECTED, 0);
            LOGGER.info("population.get(id).getNextStatusChange() = {}", population.get(id).getNextStatusChange());
        }

        // print initial conditions
        printSEIR(population, 0);

        runToCompletion(runProperties, population, contactRecords);

        Output.printSeirCSV(records);

    }

    private void printSEIR(Map<Integer, Person> population, int time) {
        Map<VirusStatus, Integer> seirCounts = PopulationGenerator.getSEIRCounts(population);

        LOGGER.info("Conditions @ time: {}", time);
        LOGGER.info("{}  {}", SUSCEPTIBLE, seirCounts.get(SUSCEPTIBLE));
        LOGGER.info("{}      {}", EXPOSED, seirCounts.get(EXPOSED));
        LOGGER.info("{}     {}", INFECTED, seirCounts.get(INFECTED));
        LOGGER.info("{}    {}", RECOVERED, seirCounts.get(RECOVERED));

        LOGGER.info("");

        SeirRecord seirRecord = new SeirRecord(time, seirCounts.get(SUSCEPTIBLE), seirCounts.get(EXPOSED), seirCounts.get(INFECTED), seirCounts.get(RECOVERED));

        records.put(time, seirRecord);

    }

    private Person getMostSevere(Person personA, Person personB) {
        VirusStatus a = personA.getStatus();
        VirusStatus b = personB.getStatus();

        return a.compareTo(b) > 0 ? personA : personB;
    }

    private void runToCompletion(StandardProperties properties, Map<Integer, Person> population, Map<Integer, List<ContactRecord>> contactRecords) {
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

    private void updatePopulationState(int time, Map<Integer, Person> population, double randomInfectionRate) {

        for (Person p : population.values()) {
            p.checkTime(time);
            if (randomInfectionRate > 0d && time > 0) {

                boolean var = rng.nextDouble() <= randomInfectionRate;
                if (var) p.randomExposure(time);
            }
        }
    }

    private void evaluateExposures(Map<Integer, Person> population, ContactRecord c, int time) {
        Person personA = getMostSevere(population.get(c.getTo()), population.get(c.getFrom()));
        Person personB = personA == population.get(c.getTo()) ? population.get(c.getFrom()) : population.get(c.getTo());

        boolean dangerMix = personA.getStatus() == INFECTED && personB.getStatus() == SUSCEPTIBLE;


        if (dangerMix && rng.nextDouble() < c.getWeight() / 30d) {
            personB.updateStatus(EXPOSED, time);
        }
    }

    private Set<Integer> infectPopulation() {

        Set<Integer> infectedIds = new HashSet<>();
        while (infectedIds.size() < runProperties.getInfected()) {
            infectedIds.add(rng.nextInt(runProperties.getPopulationSize()));
        }
        return infectedIds;

    }
}
