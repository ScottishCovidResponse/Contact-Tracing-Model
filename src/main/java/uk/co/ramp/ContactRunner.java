package uk.co.ramp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.io.CompartmentWriter;
import uk.co.ramp.io.InputFiles;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.io.csv.CsvException;
import uk.co.ramp.people.Case;
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
    public static final String COMPARTMENTS_CSV = "Compartments.csv";
        try (Reader reader = new FileReader(inputFileLocation.contactData())) {
    private StandardProperties runProperties;
    private ApplicationContext ctx;

    @Autowired
    public void setRunProperties(StandardProperties standardProperties) {
        this.runProperties = standardProperties;
    }

    @Override
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.ctx = applicationContext;
    }

    @Autowired
    public void setInputFiles(InputFiles inputFiles) {
        this.inputFiles = inputFiles;
    }

    public void run() throws IOException {

        Map<Integer, Case> population = ctx.getBean(PopulationGenerator.class).generate();
        try (Reader reader = new FileReader(inputFiles.contactData())) {

            ContactReader contactReader = ctx.getBean(ContactReader.class);

            Map<Integer, List<ContactRecord>> contactRecords = contactReader.read(reader, runProperties);

            LOGGER.info("Generated Population and Parsed Contact data");

            Outbreak infection = ctx.getBean(Outbreak.class);
            infection.setContactRecords(contactRecords);
            infection.setPopulation(population);

            LOGGER.info("Initialised Outbreak");

            Map<Integer, CmptRecord> records = infection.propagate();

            LOGGER.info("Writing Compartment Records");
            writeCompartments(new ArrayList<>(records.values()), new File(COMPARTMENTS_CSV));
            LOGGER.info("Completed. Tidying up.");
        }

    }

    void writeCompartments(List<CmptRecord> cmptRecords, File file) {
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            ctx.getBean(CompartmentWriter.class).write(bw, cmptRecords);
        } catch (IOException e) {
            String message = "An IO error occured when trying to write to " + COMPARTMENTS_CSV + ". Please ensure the file is not locked or open.";
            LOGGER.fatal(message);
            throw new CsvException(message);
        }
    }


}
