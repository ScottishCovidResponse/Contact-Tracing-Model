package uk.co.ramp;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.contact.ContactReader;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.people.Person;
import uk.co.ramp.people.PopulationGenerator;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.record.SeirRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static uk.co.ramp.people.VirusStatus.*;

public class ContactRunner {

    private static final Logger LOGGER = LogManager.getLogger(ContactRunner.class);
    Map<Integer, SeirRecord> records = new HashMap<>();

    public static void main(String[] args) {
        ContactRunner contactRunner = new ContactRunner();
        contactRunner.run();
    }

    private void run() {

        int populationSize = 10000;
        int timeLimit = 90;
        int infected = 1000;

        Map<Integer, Person> population = PopulationGenerator.generate(populationSize);
        Map<Integer, List<ContactRecord>> contactRecords = ContactReader.read(populationSize, timeLimit);


        Set<Integer> infectedIds = infectPopulation(infected, populationSize);

        LOGGER.info(infectedIds);

        for (Integer id : infectedIds) {
            population.get(id).updateStatus(INFECTED, 0);
            LOGGER.info("population.get(id).getNextStatusChange() = " + population.get(id).getNextStatusChange());
        }


        printSEIR(population, 0);

        for (int time = 0; time < timeLimit; time++) {

            updatePopulationState(time, population);
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

        printSeirCSV(records);

    }

    private void printSeirCSV(Map<Integer, SeirRecord> records) {

        try {
            FileWriter out = new FileWriter("book.csv");
            String[] HEADERS = {"Day", "S", "E", "I", "R"};
            try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(HEADERS))) {
                records.forEach((day, record) -> {
                    try {
                        printer.printRecord(day, record.getS(), record.getE(), record.getI(), record.getR());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updatePopulationState(int time, Map<Integer, Person> population) {
        for (Person p : population.values()) {
            p.checkTime(time);
        }
    }

    private void evaluateExposures(Map<Integer, Person> population, ContactRecord c, int time) {
        Person personA = getMostSevere(population.get(c.getTo()), population.get(c.getFrom()));
        Person personB = personA == population.get(c.getTo()) ? population.get(c.getFrom()) : population.get(c.getTo());

        if (personA.getStatus() == INFECTED) {
            if (personB.getStatus() == SUSCEPTIBLE) {
                if (RandomSingleton.getInstance(0).nextDouble() < c.getWeight() / 30d) {
                    personB.updateStatus(EXPOSED, time);
                }
            }
        }
    }

    private void printSEIR(Map<Integer, Person> population, int time) {
        Map<VirusStatus, Integer> seirCounts = PopulationGenerator.getSEIRCounts(population);

        records.put(time, new SeirRecord(time, seirCounts));

        LOGGER.info("Conditions @ time: " + time);

        LOGGER.info(SUSCEPTIBLE + "  " + seirCounts.get(SUSCEPTIBLE));
        LOGGER.info(EXPOSED + "      " + seirCounts.get(EXPOSED));
        LOGGER.info(INFECTED + "     " + seirCounts.get(INFECTED));
        LOGGER.info(RECOVERED + "    " + seirCounts.get(RECOVERED));

        LOGGER.info("");
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
