package uk.co.ramp.statistics;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.co.ramp.io.csv.CsvWriter;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.statistics.types.ImmutableRValueOutput;
import uk.co.ramp.statistics.types.Infection;

@Service
public class StatisticsWriter {
  private final StatisticsRecorder statisticsRecorder;
  private final StandardProperties properties;

  public StatisticsWriter(StatisticsRecorder statisticsRecorder, StandardProperties properties) {
    this.statisticsRecorder = statisticsRecorder;
    this.properties = properties;
  }

  public void output(int timeLimit, Writer statsWriter, Writer rValueWriter) throws IOException {
    outputGeneralStats(statsWriter);
    outputR(timeLimit, rValueWriter);
  }

  void outputR(int timeLimit, Writer rValueWriter) throws IOException {

    Map<Integer, List<Infection>> inf = statisticsRecorder.getR0Progression();
    MovingAverage movingAverage = new MovingAverage(7);
    List<ImmutableRValueOutput> rValueOutputs = new ArrayList<>();
    for (int i = 0; i < timeLimit; i++) {
      List<Infection> orDefault = inf.get(i);
      if (orDefault != null) {
        int seeded = orDefault.stream().mapToInt(Infection::infections).sum();
        double r = seeded / (double) orDefault.size();
        movingAverage.add(r);

        rValueOutputs.add(
            ImmutableRValueOutput.builder()
                .time(i)
                .newInfectors(orDefault.size())
                .newInfections(seeded)
                .r(r)
                .sevenDayAverageR(movingAverage.getAverage())
                .build());
      }
    }
    new CsvWriter().write(rValueWriter, rValueOutputs, ImmutableRValueOutput.class);
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
  }
}
