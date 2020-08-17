package uk.co.ramp.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.ramp.TestUtils;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.statistics.types.ImmutableInfection;
import uk.co.ramp.statistics.types.ImmutableRValueOutput;
import uk.co.ramp.statistics.types.Infection;

public class StatisticsWriterTest {

  private StatisticsWriter statisticsWriter;
  private final Random random = TestUtils.getRandom();
  private List<ImmutableRValueOutput> rollingAverage = new ArrayList<>();

  @Before
  public void setup() {
    StatisticsRecorder statisticsRecorder = Mockito.mock(StatisticsRecorder.class);
    StandardProperties properties = Mockito.mock(StandardProperties.class);
    when(properties.timeStepsPerDay()).thenReturn(1);
    statisticsWriter = new StatisticsWriter(statisticsRecorder, properties);

    Map<Integer, List<Infection>> infections = new HashMap<>();
    for (int i = 0; i < 10; i++) {
      int exposedTime = random.nextInt(3);
      int furtherInfections = random.nextInt(10);
      Case seedCase = Mockito.mock(Case.class);
      when(seedCase.id()).thenReturn(i);
      when(seedCase.exposedTime()).thenReturn(exposedTime);
      Infection infection =
          ImmutableInfection.builder().seed(i).infections(furtherInfections).build();
      infections.compute(
          exposedTime,
          (k, v) ->
              v == null
                  ? List.of(infection)
                  : Stream.of(List.of(infection), v)
                      .flatMap(Collection::stream)
                      .collect(Collectors.toList()));
    }

    when(statisticsRecorder.getR0Progression()).thenReturn(infections);

    Map<Integer, Integer> map1 = createMap(10);
    when(statisticsRecorder.getPersonDaysIsolation()).thenReturn(map1);
    Map<Integer, Integer> map2 = createMap(10);
    when(statisticsRecorder.getPeopleInfected()).thenReturn(map2);
    Map<Integer, Integer> map3 = createMap(10);

    when(statisticsRecorder.getContactsTraced()).thenReturn(map3);

    for (int i = 0; i < 5; i++)
      rollingAverage.add(
          ImmutableRValueOutput.builder()
              .r(random.nextDouble())
              .sevenDayAverageR(random.nextDouble())
              .newInfections(random.nextInt(5))
              .newInfectors(random.nextInt(5))
              .time(i)
              .build());

    when(statisticsRecorder.getRollingAverage(7)).thenReturn(rollingAverage);
  }

  private Map<Integer, Integer> createMap(int keyMax) {
    Map<Integer, Integer> map = new HashMap<>();
    for (int i = 0; i < keyMax; i++) {
      map.put(i, random.nextInt(keyMax));
    }
    return map;
  }

  @Test
  public void outputR() throws IOException {

    Writer writer = new StringWriter();
    statisticsWriter.outputR(writer);
    List<String> output = List.of(writer.toString().split("\n"));
    assertThat(output.get(0))
        .contains("\"time\",\"newInfectors\",\"newInfections\",\"r\",\"sevenDayAverageR\"");

    for (int i = 1; i < output.size(); i++) {
      var outputLine = output.get(i);
      var data = rollingAverage.get(i - 1);
      assertThat(outputLine)
          .contains(String.valueOf(data.r()))
          .contains(String.valueOf(data.sevenDayAverageR()))
          .contains(String.valueOf(data.newInfections()))
          .contains(String.valueOf(data.newInfectors()));
    }
  }

  @Test
  public void outputGeneralStats() throws IOException {

    Writer writer = new StringWriter();
    statisticsWriter.outputGeneralStats(writer);

    assertThat(writer.toString()).contains("Person Days in Isolation ,");
    assertThat(writer.toString()).contains("People infected ,");
    assertThat(writer.toString()).contains("Contacts Traced ,");
    assertThat(writer.toString()).contains("Correct Positive Tests ,");
    assertThat(writer.toString()).contains("Correct Negative Tests ,");
    assertThat(writer.toString()).contains("False Positive Tests ,");
    assertThat(writer.toString()).contains("False Negative Tests ,");
  }
}
