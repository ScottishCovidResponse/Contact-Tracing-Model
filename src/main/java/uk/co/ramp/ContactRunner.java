package uk.co.ramp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uk.co.ramp.event.CompletionEventListGroup;
import uk.co.ramp.io.CompartmentWriter;
import uk.co.ramp.io.ContactReader;
import uk.co.ramp.io.csv.CsvException;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.io.types.InputFiles;
import uk.co.ramp.io.types.OutputFolder;

@Service
public class ContactRunner implements CommandLineRunner {

  private static final Logger LOGGER = LogManager.getLogger(ContactRunner.class);
  public static final String COMPARTMENTS_CSV = "Compartments.csv";
  private InputFiles inputFileLocation;
  private File outputFolder;
  private ApplicationContext ctx;

  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.ctx = applicationContext;
  }

  @Autowired
  public void setInputFileLocation(InputFiles inputFileLocation) {
    this.inputFileLocation = inputFileLocation;
  }

  @Autowired
  public void setOutputFolder(OutputFolder outputFolder) {
    this.outputFolder = outputFolder.outputFolder();
  }

  @Override
  public void run(String... args) throws IOException {

    try (Reader reader = new FileReader(inputFileLocation.contactData())) {

      ContactReader contactReader = ctx.getBean(ContactReader.class);
      CompletionEventListGroup eventList = ctx.getBean(CompletionEventListGroup.class);

      eventList.addNewContactEvents(contactReader.readEvents(reader));

      LOGGER.info("Generated Population and Parsed Contact data");

      Outbreak infection = ctx.getBean(Outbreak.class);

      LOGGER.info("Initialised Outbreak");

      Map<Integer, CmptRecord> records = infection.propagate();

      LOGGER.info("Writing Compartment Records");
      writeCompartments(
          new ArrayList<>(records.values()), new File(outputFolder, COMPARTMENTS_CSV));
      LOGGER.info("Completed. Tidying up.");
    }
  }

  void writeCompartments(List<CmptRecord> cmptRecords, File file) {
    try (FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw)) {
      ctx.getBean(CompartmentWriter.class).write(bw, cmptRecords);
    } catch (IOException e) {
      String message =
          "An IO error occured when trying to write to "
              + COMPARTMENTS_CSV
              + ". Please ensure the file is not locked or open.";
      LOGGER.fatal(message);
      throw new CsvException(message);
    }
  }
}
