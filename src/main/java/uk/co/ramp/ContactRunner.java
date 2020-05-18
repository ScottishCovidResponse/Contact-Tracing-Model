package uk.co.ramp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.io.SeirWriter;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.people.Person;
import uk.co.ramp.people.PopulationGenerator;
import uk.co.ramp.record.CmptRecord;
import uk.co.ramp.utilities.ContactReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ContactRunner implements ApplicationContextAware {

    private static final Logger LOGGER = LogManager.getLogger(ContactRunner.class);

    private StandardProperties runProperties;
    private ApplicationContext ctx;

    public ContactRunner() {
    }

    @Autowired
    public ContactRunner(StandardProperties runProperties) {
        this.runProperties = runProperties;
    }

    public void run() throws IOException {

        Map<Integer, Person> population = ctx.getBean(PopulationGenerator.class).generate();
        try (Reader reader = new FileReader(runProperties.contactsFile())) {
            Map<Integer, List<ContactRecord>> contactRecords = new ContactReader().read(reader, runProperties);

            LOGGER.info("Generated Population and Parsed Contact data");

            Outbreak infection = ctx.getBean(Outbreak.class);
            infection.setContactRecords(contactRecords);
            infection.setPopulation(population);

            Map<Integer, CmptRecord> records = infection.propagate();

            writeSEIR(new ArrayList<>(records.values()), new File("SEIR.csv"));
        }

    }

    void writeSEIR(List<CmptRecord> cmptRecords, File file) {
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            new SeirWriter().write(bw, cmptRecords);
        } catch (IOException e) {
            LOGGER.fatal("Could not write SEIR");
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.ctx = applicationContext;
    }

}