package uk.co.ramp.statistics;

import java.io.IOException;
import java.io.Writer;
import uk.co.ramp.io.csv.CsvWriter;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.statistics.types.ImmutableRValueOutput;

public class StatisticsWriter {
  private final StatisticsRecorder statisticsRecorder;
  private final StandardProperties properties;

  public StatisticsWriter(StatisticsRecorder statisticsRecorder, StandardProperties properties) {
    this.statisticsRecorder = statisticsRecorder;
    this.properties = properties;
  }

  public void output(Writer statsWriter, Writer rValueWriter) throws IOException {
    outputGeneralStats(statsWriter);
    outputR(rValueWriter);
  }

  void outputR(Writer rValueWriter) throws IOException {

    new CsvWriter()
        .write(rValueWriter, statisticsRecorder.getRollingAverage(7), ImmutableRValueOutput.class);
  }

  void outputGeneralStats(Writer statsWriter) throws IOException {

    double timeIsolated =
        statisticsRecorder.getPersonDaysIsolation().values().stream()
                .mapToInt(Integer::intValue)
                .sum()
            / (double) properties.timeStepsPerDay();
    int totalInfected =
        statisticsRecorder.getPeopleInfected().values().stream().mapToInt(Integer::intValue).sum();
    int contactsTraced =
        statisticsRecorder.getContactsTraced().values().stream().mapToInt(Integer::intValue).sum();

    statsWriter.write("Person Days in Isolation , " + timeIsolated);
    statsWriter.write("\nPeople infected , " + totalInfected);
    statsWriter.write("\nContacts Traced , " + contactsTraced);
    statsWriter.write("\nCorrect Positive Tests , " + statisticsRecorder.getTruePositives());
    statsWriter.write("\nCorrect Negative Tests , " + statisticsRecorder.getTrueNegatives());
    statsWriter.write("\nFalse Positive Tests , " + statisticsRecorder.getFalsePositives());
    statsWriter.write("\nFalse Negative Tests , " + statisticsRecorder.getFalseNegatives());
  }
}
