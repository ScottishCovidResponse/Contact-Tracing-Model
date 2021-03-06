package uk.co.ramp.statistics;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.ramp.TestUtils;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.Case;
import uk.co.ramp.statistics.types.ImmutableInfection;
import uk.co.ramp.statistics.types.Infection;

public class StatisticsRecorderImplTest {

  private final Random random = TestUtils.getRandom();
  private final StandardProperties properties = TestUtils.standardProperties();
  private StatisticsRecorder recorder;

  @Before
  public void setUp() {
    recorder =
        new StatisticsRecorderImpl(
            properties,
            new HashMap<>(),
            new HashMap<>(),
            new HashMap<>(),
            new HashMap<>(),
            new EnumMap<>(AlertStatus.class),
            new EnumMap<>(AlertStatus.class));
  }

  @Test
  public void testIncorrectResults() {

    // Avoid NPE
    assertThat(recorder.getFalseNegatives()).isNotNull().isZero();
    assertThat(recorder.getFalsePositives()).isNotNull().isZero();

    int falsePos = random.nextInt(100);
    int falseNeg = random.nextInt(100);

    IntStream.range(0, falsePos)
        .forEach(i -> recorder.recordIncorrectTestResult(AlertStatus.TESTED_POSITIVE));
    IntStream.range(0, falseNeg)
        .forEach(i -> recorder.recordIncorrectTestResult(AlertStatus.TESTED_NEGATIVE));

    assertThat(recorder.getFalsePositives()).isEqualTo(falsePos);
    assertThat(recorder.getFalseNegatives()).isEqualTo(falseNeg);
  }

  @Test
  public void testCorrectResults() {

    // Avoid NPE
    assertThat(recorder.getFalseNegatives()).isNotNull().isZero();
    assertThat(recorder.getFalsePositives()).isNotNull().isZero();

    int truePos = random.nextInt(100);
    int trueNeg = random.nextInt(100);

    IntStream.range(0, truePos)
        .forEach(i -> recorder.recordCorrectTestResult(AlertStatus.TESTED_POSITIVE));
    IntStream.range(0, trueNeg)
        .forEach(i -> recorder.recordCorrectTestResult(AlertStatus.TESTED_NEGATIVE));

    assertThat(recorder.getTruePositives()).isEqualTo(truePos);
    assertThat(recorder.getTrueNegatives()).isEqualTo(trueNeg);
  }

  @Test
  public void recordDaysInIsolation() {
    int d1 = random.nextInt(100);
    int d2 = random.nextInt(100);

    recorder.recordDaysInIsolation(0, d1);
    recorder.recordDaysInIsolation(0, d2);

    assertThat(recorder.getPersonDaysIsolation().get(0)).isEqualTo(d1 + d2);
    assertThat(recorder.getPersonDaysIsolation().get(1)).isNull();
  }

  @Test
  public void recordPeopleInfected() {

    Map<Integer, Integer> dummyData = new HashMap<>();
    for (int i = 0; i < 10; i++) {
      int times = random.nextInt(20);
      dummyData.put(i, times);

      for (int j = 0; j < times; j++) recorder.recordSinglePersonInfected(i);
    }

    assertThat(dummyData).isEqualTo(recorder.getPeopleInfected());
    assertThat(recorder.getPeopleInfected().get(11)).isNull();
  }

  @Test
  public void recordContactsTraced() {
    int sum = 0;
    for (int i = 0; i < 10; i++) {
      int time = random.nextInt(3);
      int contacts = random.nextInt(6);
      sum += contacts;
      recorder.recordContactsTraced(time, contacts);
    }
    assertThat(recorder.getContactsTraced().values().stream().mapToInt(Integer::intValue).sum())
        .isEqualTo(sum);
  }

  @Test
  public void recordInfectionSpread() {

    Map<Integer, List<Infection>> infections = new HashMap<>();
    Map<Integer, Case> cases = new HashMap<>();
    Map<Integer, Integer> further = new HashMap<>();

    for (int i = 0; i < 10; i++) {
      int exposedTime = random.nextInt(3);
      int furtherInfections = random.nextInt(10);
      Case seedCase = Mockito.mock(Case.class);
      when(seedCase.id()).thenReturn(i);
      when(seedCase.exposedTime()).thenReturn(exposedTime);
      cases.put(i, seedCase);
      further.put(i, furtherInfections);
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

    for (Map.Entry<Integer, Case> entry : cases.entrySet()) {
      recorder.recordInfectionSpread(entry.getValue(), further.get(entry.getKey()));
    }

    Map<Integer, List<Infection>> result = recorder.getR0Progression();

    for (Integer key : result.keySet()) {
      List<Infection> resultSet = result.get(key);
      assertThat(resultSet.size()).isEqualTo(infections.get(key).size());

      for (Infection infection : resultSet) {
        assertThat(infections.get(key).contains(infection));
      }
    }
  }
}
