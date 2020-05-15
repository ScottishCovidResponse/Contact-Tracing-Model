package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.contact.ContactException;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.ImmutableStandardProperties;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.io.SeirWriter;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.io.readers.DiseasePropertiesReader;
import uk.co.ramp.io.readers.PopulationPropertiesReader;
import uk.co.ramp.io.readers.StandardPropertiesReader;
import uk.co.ramp.people.Person;
import uk.co.ramp.people.PopulationGenerator;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.record.ImmutableSeirRecord;
import uk.co.ramp.record.SeirRecord;
import uk.co.ramp.utilities.ContactReader;
import uk.co.ramp.utilities.RandomSingleton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.co.ramp.people.VirusStatus.EXPOSED;
import static uk.co.ramp.people.VirusStatus.INFECTED;
import static uk.co.ramp.people.VirusStatus.RECOVERED;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

public class ContactRunner {

    private static final Logger LOGGER = LogManager.getLogger(ContactRunner.class);
    private static RandomDataGenerator rng;
    private final Map<Integer, SeirRecord> records = new HashMap<>();

    private static PopulationProperties populationProperties;
    private static StandardProperties runProperties;
    private static DiseaseProperties diseaseProperties;

    public ContactRunner() {
    }

    // for testing
    public ContactRunner(StandardProperties standardProperties, DiseaseProperties diseaseProps, PopulationProperties populationProps) {

        runProperties = standardProperties;
        diseaseProperties = diseaseProps;
        populationProperties = populationProps;

        rng = new RandomDataGenerator();
    }

    public static void main(String[] args) throws IOException {

        readInputs();
        ContactRunner contactRunner = new ContactRunner();
        rng = RandomSingleton.getInstance(runProperties.seed());
        Optional<Integer> sid = Optional.empty();
        if (args.length == 1) {
            sid = Optional.of(Integer.parseInt(args[0]));
        }
        contactRunner.run(sid);
    }


    private static void readInputs() throws IOException {
        try (FileReader fr = new FileReader(new File("input/runSettings.json"));
             BufferedReader br = new BufferedReader(fr)) {
            runProperties = new StandardPropertiesReader().read(br);
        }

        try (FileReader fr = new FileReader(new File("input/populationSettings.json"));
             BufferedReader br = new BufferedReader(fr)) {
            populationProperties = new PopulationPropertiesReader().read(br);
        }

        try (FileReader fr = new FileReader(new File("input/diseaseSettings.json"));
             BufferedReader br = new BufferedReader(fr)) {
            diseaseProperties = new DiseasePropertiesReader().read(br);
        }
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

                Person potentialSpreader = population.get(contacts.to());
                Person victim = population.get(contacts.from());

                if (potentialSpreader.getStatus() != victim.getStatus()) {
                    evaluateExposures(population, contacts, time);
                }
            }
        }
    }

    public static RandomDataGenerator getRng() {
        return rng;
    }

    private void run(Optional<Integer> sidCmd) throws IOException {

        // readInputs();

        if (sidCmd.isPresent()) {
            int seed = runProperties.seed();
            LOGGER.warn("The seed from input file will be modified from {} to {}", sidCmd.get(), seed + sidCmd.get());
            seed += sidCmd.get();
            runProperties = ImmutableStandardProperties.copyOf(runProperties).withSeed(seed);
        }


        Map<Integer, Person> population = new PopulationGenerator(runProperties, populationProperties).generate();
        Map<Integer, List<ContactRecord>> contactRecords = readContacts(new File("input/contacts.csv"), runProperties);
        Set<Integer> infectedIds = infectPopulation();

        LOGGER.info(infectedIds);

        for (Integer id : infectedIds) {
            population.get(id).updateStatus(INFECTED, 0);
            LOGGER.info("population.get(id).getNextStatusChange() = {}", population.get(id).getNextStatusChange());
        }

        // print initial conditions
        printSEIR(population, 0);

        runToCompletion(runProperties, population, contactRecords);

        writeSEIR(new ArrayList<>(records.values()), new File("SEIR.csv"));
    }

    Map<Integer, List<ContactRecord>> readContacts(File file, StandardProperties runProperties) {
        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {
            return new ContactReader().read(br, runProperties);
        } catch (IOException e) {
            LOGGER.fatal("Could not read contacts");
            throw new ContactException(e.getMessage());
        }
    }

    void writeSEIR(List<SeirRecord> seirRecords, File file) {
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            new SeirWriter().write(bw, seirRecords);
        } catch (IOException e) {
            LOGGER.fatal("Could not write SEIR");
            throw new RuntimeException(e.getMessage());
        }
    }

    private void printSEIR(Map<Integer, Person> population, int time) {
        Map<VirusStatus, Integer> seirCounts = PopulationGenerator.getSEIRCounts(population);

        LOGGER.debug("Conditions @ time: {}", time);
        LOGGER.debug("{}  {}", SUSCEPTIBLE, seirCounts.get(SUSCEPTIBLE));
        LOGGER.debug("{}      {}", EXPOSED, seirCounts.get(EXPOSED));
        LOGGER.debug("{}     {}", INFECTED, seirCounts.get(INFECTED));
        LOGGER.debug("{}    {}", RECOVERED, seirCounts.get(RECOVERED));

        SeirRecord seirRecord = ImmutableSeirRecord.builder()
                .time(time)
                .s(seirCounts.get(SUSCEPTIBLE))
                .e(seirCounts.get(EXPOSED))
                .i(seirCounts.get(INFECTED))
                .r(seirCounts.get(RECOVERED))
                .build();

        records.put(time, seirRecord);

    }

    private Person getMostSevere(Person personA, Person personB) {
        VirusStatus a = personA.getStatus();
        VirusStatus b = personB.getStatus();

        return a.compareTo(b) > 0 ? personA : personB;
    }

    private void runToCompletion(StandardProperties properties, Map<Integer, Person> population, Map<Integer, List<ContactRecord>> contactRecords) {
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

    private void updatePopulationState(int time, Map<Integer, Person> population, double randomInfectionRate) {

        for (Person p : population.values()) {
            p.checkTime(time);
            if (p.getStatus() == SUSCEPTIBLE && randomInfectionRate > 0d && time > 0) {

                boolean var = rng.nextUniform(0, 1) <= randomInfectionRate;
                if (var) p.randomExposure(time);
            }
        }
    }

    private void evaluateExposures(Map<Integer, Person> population, ContactRecord c, int time) {
        Person personA = getMostSevere(population.get(c.to()), population.get(c.from()));
        Person personB = personA == population.get(c.to()) ? population.get(c.from()) : population.get(c.to());

        boolean dangerMix = personA.getStatus() == INFECTED && personB.getStatus() == SUSCEPTIBLE;

        if (dangerMix && rng.nextUniform(0, 1) < c.weight() / diseaseProperties.exposureTuning()) {
            personB.updateStatus(EXPOSED, time, personA.getId());
        }
    }

    private Set<Integer> infectPopulation() {

        Set<Integer> infectedIds = new HashSet<>();
        while (infectedIds.size() < runProperties.infected()) {
            infectedIds.add(rng.nextInt(0, runProperties.populationSize()));
        }
        return infectedIds;

    }
}
