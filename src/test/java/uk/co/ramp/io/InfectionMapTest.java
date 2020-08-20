package uk.co.ramp.io;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.*;
import static uk.co.ramp.people.VirusStatus.RECOVERED;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.core.util.StringBuilderWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.ramp.LogSpy;
import uk.co.ramp.TestUtils;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.statistics.StatisticsRecorder;

public class InfectionMapTest {

  private final Random random = TestUtils.getRandom();
  private final Map<VirusStatus, Integer> counts = new EnumMap<>(VirusStatus.class);
  private final int populationSize = 100;
  @Rule public LogSpy logSpy = new LogSpy();
  private InfectionMap infectionMap;
  private Set<Integer> seeds;

  @Before
  public void setup() {
    Map<Integer, Case> population = new HashMap<>();
    seeds = new HashSet<>();

    var statuses = VirusStatus.values();
    var count = statuses.length;

    for (int i = 0; i < 10; i++) {
      seeds.add(random.nextInt(populationSize));
    }

    for (int i = 0; i < populationSize; i++) {

      if (seeds.contains(i)) {
        Case thisCase = mock(Case.class);
        when(thisCase.id()).thenReturn(i);
        when(thisCase.virusStatus()).thenReturn(RECOVERED);
        when(thisCase.exposedBy()).thenReturn(Case.getInitial());
        population.put(i, thisCase);
      } else {
        VirusStatus virusStatus = statuses[random.nextInt(count)];
        int infector = new ArrayList<>(seeds).get(random.nextInt(seeds.size()));

        counts.merge(virusStatus, 1, (prev, one) -> prev + 1);

        Case thisCase = mock(Case.class);
        when(thisCase.id()).thenReturn(i);
        when(thisCase.virusStatus()).thenReturn(virusStatus);
        when(thisCase.exposedBy()).thenReturn(infector);
        population.put(i, thisCase);
      }
    }
    StandardProperties properties = Mockito.mock(StandardProperties.class);
    when(properties.timeStepsPerDay()).thenReturn(1);
    infectionMap = new InfectionMap(population, Mockito.mock(StatisticsRecorder.class), properties);
  }

  @Test
  public void outputMap() {

    StringWriter stringWriter = new StringWriter();
    infectionMap.outputMap(stringWriter);

    Set<Integer> lines =
        Arrays.stream(stringWriter.toString().split("\n"))
            .filter(s -> !s.isBlank())
            .map(s -> s.substring(0, s.indexOf("(")))
            .map(Integer::parseInt)
            .collect(Collectors.toSet());

    Assert.assertEquals(lines, seeds);
  }

  @Test
  public void testException() throws IOException {

    infectionMap = mock(InfectionMap.class);
    doCallRealMethod().when(infectionMap).outputMap(any());
    doThrow(new IOException(""))
        .when(infectionMap)
        .recurseSet(anyList(), anyMap(), any(), anyInt());

    assertThatExceptionOfType(InfectionMapException.class)
        .isThrownBy(() -> infectionMap.outputMap(new StringWriter()))
        .withMessageContaining("An error occurred while writing the map file");
  }

  @Test
  public void collectInfectors() {

    Map<Integer, List<Case>> temp = infectionMap.collectInfectors();
    Assert.assertEquals(seeds.size(), temp.get(Case.getInitial()).size());

    int sum = seeds.stream().mapToInt(seed -> temp.get(seed).size()).sum();

    Assert.assertEquals(sum, populationSize - counts.get(SUSCEPTIBLE) - seeds.size());
  }

  @Test
  public void recurseSet() throws IOException {

    int tab = 1;
    Map<Integer, List<Case>> infectors = new HashMap<>();

    Case root = mock(Case.class);
    when(root.id()).thenReturn(0);
    when(root.exposedBy()).thenReturn(Case.getInitial());
    when(root.exposedTime()).thenReturn(10);

    Case first = mock(Case.class);
    when(first.id()).thenReturn(1);
    when(first.exposedBy()).thenReturn(0);
    when(first.exposedTime()).thenReturn(10);
    infectors.putIfAbsent(root.id(), List.of(first));

    Case second = mock(Case.class);
    when(second.id()).thenReturn(2);
    when(second.exposedBy()).thenReturn(1);
    when(second.exposedTime()).thenReturn(10);
    infectors.putIfAbsent(first.id(), List.of(second));

    List<Case> target = List.of(root);
    Writer writer = new StringBuilderWriter();
    infectionMap.recurseSet(target, infectors, writer, tab);

    String expected =
        "0(10)         ->  [1(10)]\n" + "              ->  1(10)          ->  [2(10)]";

    Assert.assertEquals(expected, writer.toString().trim());
  }
}
