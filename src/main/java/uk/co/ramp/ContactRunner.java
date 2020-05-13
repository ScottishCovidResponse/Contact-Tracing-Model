package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
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
import uk.co.ramp.record.SeirRecord;
import uk.co.ramp.utilities.ContactReader;
import uk.co.ramp.utilities.RandomSingleton;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ContactRunner {

    private static final Logger LOGGER = LogManager.getLogger(ContactRunner.class);
    private static RandomDataGenerator rng;

    private static PopulationProperties populationProperties;
    private static StandardProperties runProperties;
    private static DiseaseProperties diseaseProperties;

    public static void main(String[] args) throws IOException {

        readInputs();
        ContactRunner contactRunner = new ContactRunner();
        rng = RandomSingleton.getInstance(runProperties.getSeed());

        // if a cmd line arg is an is, parse it as a string
        int sid = 0;
        if (args.length == 1) {
            sid = Integer.parseInt(args[0]);
        }

        if (sid != 0) {
            int seed = runProperties.getSeed();
            LOGGER.warn("The seed from input file will be modified from {} to {}", sid, seed + sid);
            seed += sid;
            runProperties.setSeed(seed);
        }

        contactRunner.run();
    }


    public void run() {
        Map<Integer, Person> population = new PopulationGenerator(runProperties, populationProperties).generate();
        Map<Integer, List<ContactRecord>> contactRecords = ContactReader.read(runProperties);

        Infection infection = new Infection(population, contactRecords, runProperties, diseaseProperties, rng);

        Map<Integer, SeirRecord> records = infection.propagate();
        Output.printSeirCSV(records);

    }

    private static void readInputs() throws IOException {
        runProperties = StandardPropertiesReader.read(new File("input/runSettings.json"));
        populationProperties = PopulationPropertiesReader.read(new File("input/populationSettings.json"));
        diseaseProperties = DiseasePropertiesReader.read(new File("input/diseaseSettings.json"));
    }

    public static RandomDataGenerator getRng() {
        return rng;
    }

    public static DiseaseProperties getDiseaseProperties() {
        return diseaseProperties;
    }

}