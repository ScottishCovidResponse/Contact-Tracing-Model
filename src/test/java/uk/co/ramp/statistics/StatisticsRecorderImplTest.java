package uk.co.ramp.statistics;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

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
import uk.co.ramp.statistics.types.Infection;

public class StatisticsRecorderImplTest {

  StatisticsRecorder recorder;
  Random random = TestUtils.getRandom();
  StandardProperties properties = TestUtils.standardProperties();

  @Before
  public void setUp() {
    recorder =
        new StatisticsRecorderImpl(
            properties, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
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
