package uk.co.ramp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.io.Output;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.people.Person;
import uk.co.ramp.people.PopulationGenerator;
import uk.co.ramp.record.SeirRecord;
import uk.co.ramp.utilities.ContactReader;

import java.util.List;
import java.util.Map;

@Service
public class ContactRunner implements ApplicationContextAware {

    private static final Logger LOGGER = LogManager.getLogger(ContactRunner.class);

    private PopulationProperties populationProperties;
    private StandardProperties runProperties;
    private ApplicationContext ctx;

    public ContactRunner() {
    }

    @Autowired
    public ContactRunner(PopulationProperties populationProperties, StandardProperties runProperties) {
        this.populationProperties = populationProperties;
        this.runProperties = runProperties;
    }

    public void run() {

        Map<Integer, Person> population = new PopulationGenerator(runProperties, populationProperties).generate();
        Map<Integer, List<ContactRecord>> contactRecords = ContactReader.read(runProperties);

        LOGGER.info("Generated Population and Parsed Contact data");

        Outbreak infection = ctx.getBean(Outbreak.class);
        infection.setContactRecords(contactRecords);
        infection.setPopulation(population);

        Map<Integer, SeirRecord> records = infection.propagate();
        Output.printSeirCSV(records);

    }

    @Override
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.ctx = applicationContext;
    }

}