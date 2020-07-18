package uk.co.ramp;

import static uk.co.ramp.people.VirusStatus.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.event.EventListWriter;
import uk.co.ramp.event.LastContactTime;
import uk.co.ramp.event.types.EventRunner;
import uk.co.ramp.io.InfectionMap;
import uk.co.ramp.io.InfectionMapException;
import uk.co.ramp.io.LogDailyOutput;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.OutputFolder;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.VirusStatus;

@Service
public class Outbreak {

  private static final Logger LOGGER = LogManager.getLogger(Outbreak.class);

  private final StandardProperties properties;
  private final DiseaseProperties diseaseProperties;
  private final EventRunner eventRunner;
  private final EventListWriter eventListWriter;
  private final LogDailyOutput outputLog;
  private final LastContactTime lastContactTime;
  private final File outputFolder;

  private final Population population;
  private final Map<Integer, CmptRecord> records = new HashMap<>();
  private static final String INFECTION_MAP = "infectionMap.txt";

  @Autowired
  public Outbreak(
      Population population,
      DiseaseProperties diseaseProperties,
      StandardProperties standardProperties,
      LogDailyOutput outputLog,
      EventRunner eventRunner,
      EventListWriter eventListWriter,
      LastContactTime lastContactTime,
      OutputFolder outputFolder) {

    this.population = population;
    this.diseaseProperties = diseaseProperties;
    this.properties = standardProperties;
    this.outputLog = outputLog;
    this.eventRunner = eventRunner;
    this.eventListWriter = eventListWriter;
    this.lastContactTime = lastContactTime;
    this.outputFolder = outputFolder.outputFolder();
  }

  public Map<Integer, CmptRecord> propagate() {
    runToCompletion();
    return records;
  }

  void runToCompletion() {
    // the latest time to run to
    int timeLimit = properties.timeLimit();
    double randomInfectionRate =
        diseaseProperties.randomInfectionRate() / (double) properties.timeStepsPerDay();

    runContactData(timeLimit * properties.timeStepsPerDay(), randomInfectionRate);

    try (Writer writer = new FileWriter(new File(outputFolder, INFECTION_MAP))) {
      new InfectionMap(population.view()).outputMap(writer);
      eventListWriter.output();
    } catch (IOException e) {
      String message = "An error occurred generating the infection map";
      LOGGER.error(message);
      throw new InfectionMapException(message, e);
    }
  }

  void runContactData(int timeLimit, double randomInfectionRate) {
    int lastContact = lastContactTime.get();

    if (lastContact > timeLimit) {
      LOGGER.info("timeLimit it lower than time of last contact event");
      LOGGER.info("Not all contact data will be used");
    }

    for (int time = 0; time <= timeLimit; time++) {

      eventRunner.run(time, randomInfectionRate, lastContact);
      updateLogActiveCases(time);

      // stop random infections after contacts end
      if (activeCases() == 0 && lastContact < time) {
        randomInfectionRate = 0d;
      }

      if (activeCases() == 0 && randomInfectionRate == 0d) {
        LOGGER.info("There are no active cases and the random infection rate is zero.");
        LOGGER.info("Exiting as solution is stable.");
        break;
      }
    }
  }

  void updateLogActiveCases(int time) {
    Map<VirusStatus, Integer> stats = population.getCmptCounts();
    CmptRecord cmptRecord = outputLog.log(time, stats);
    records.put(time, cmptRecord);
  }

  private int activeCases() {
    var stats = population.getCmptCounts();
    return stats.get(EXPOSED)
        + stats.get(ASYMPTOMATIC)
        + stats.get(PRESYMPTOMATIC)
        + stats.get(SYMPTOMATIC)
        + stats.get(SEVERELY_SYMPTOMATIC);
  }
}
