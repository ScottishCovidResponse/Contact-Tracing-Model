package uk.co.ramp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.io.Output;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.io.StandardProperties;
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
    private final Map<Integer, SeirRecord> records = new HashMap<>();
    private int sid;

    public static void main(String[] args) throws IOException {
        ContactRunner contactRunner = new ContactRunner();

        Optional<Integer> sid = Optional.empty();
        if (args.length == 1) {
            sid = Optional.of(Integer.parseInt(args[0]));
        }
        contactRunner.run(sid);
    }

    private void run(Optional<Integer> sidCmd) throws IOException {

        StandardProperties properties = StandardPropertiesReader.read(new File("runSettings.json"));
        PopulationProperties populationProperties = PopulationPropertiesReader.read(new File("population.json"));

        int populationSize = properties.getPopulationSize();
        int timeLimit = properties.getTimeLimit();
        int infected = properties.getInfected();
        int sidTemp = properties.getSid();

        if (sidCmd.isPresent()) {
            LOGGER.warn("SID from input file has been overridden by the command line variable {}", sidCmd.get());
            sid = sidCmd.get();
        } else {
            sid = sidTemp;
        }


        Map<Integer, Person> population = PopulationGenerator.generate(populationSize, populationProperties, sid);
        Map<Integer, List<ContactRecord>> contactRecords = ContactReader.read(populationSize, timeLimit);

        Set<Integer> infectedIds = infectPopulation(infected, populationSize);

        LOGGER.info(infectedIds);

        for (Integer id : infectedIds) {
            population.get(id).updateStatus(INFECTED, 0);
            LOGGER.info("population.get(id).getNextStatusChange() = {}", population.get(id).getNextStatusChange());
        }

        // print initial conditions
        printSEIR(population, 0);

        runToCompletion(properties, population, contactRecords);

        Output.printSeirCSV(records);

    }

    private void runToCompletion(StandardProperties properties, Map<Integer, Person> population, Map<Integer, List<ContactRecord>> contactRecords) {
        int timeLimit = properties.getTimeLimit();
        int maxContact = contactRecords.keySet().stream().max(Comparator.naturalOrder()).orElseThrow(RuntimeException::new);
        int runTime;
        boolean steadyState = properties.isSteadyState();
        double randomInfectionRate = properties.getRandomInfectionRate();

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


    private void updatePopulationState(int time, Map<Integer, Person> population, double randomInfectionRate) {
        for (Person p : population.values()) {
            p.checkTime(time);
            if (randomInfectionRate > 0d && time > 0) {

                boolean var = RandomSingleton.getInstance(sid).nextDouble() <= randomInfectionRate;
                if (var) p.randomExposure(time);
            }
        }
    }

    private void evaluateExposures(Map<Integer, Person> population, ContactRecord c, int time) {
        Person personA = getMostSevere(population.get(c.getTo()), population.get(c.getFrom()));
        Person personB = personA == population.get(c.getTo()) ? population.get(c.getFrom()) : population.get(c.getTo());

        boolean dangerMix = personA.getStatus() == INFECTED && personB.getStatus() == SUSCEPTIBLE;

        if (dangerMix && RandomSingleton.getInstance(0).nextDouble() < c.getWeight() / 30d) {
            personB.updateStatus(EXPOSED, time);
        }
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
        System.out.println(seirRecord.toString());

        records.put(time, seirRecord);
        seirRecord = null;


    }

    private Person getMostSevere(Person personA, Person personB) {
        VirusStatus a = personA.getStatus();
        VirusStatus b = personB.getStatus();

        return a.compareTo(b) > 0 ? personA : personB;
    }

    private Set<Integer> infectPopulation(int infected, int populationSize) {

        Set<Integer> infectedIds = new HashSet<>();
        while (infectedIds.size() < infected) {
            infectedIds.add(RandomSingleton.getInstance(0).nextInt(populationSize));
        }
        return infectedIds;

    }
}
