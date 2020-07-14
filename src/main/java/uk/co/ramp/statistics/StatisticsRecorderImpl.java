package uk.co.ramp.statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.co.ramp.people.Case;
import uk.co.ramp.statistics.types.ImmutableInfection;
import uk.co.ramp.statistics.types.Infection;

@Service
public class StatisticsRecorderImpl implements StatisticsRecorder {

  private Map<Integer, Integer> personDaysIsolation = new HashMap<>();
  private Map<Integer, Integer> peopleInfected = new HashMap<>();
  private Map<Integer, Integer> contactsTraced = new HashMap<>();
  private Map<Integer, List<Infection>> r0Progression = new HashMap<>();

  public Map<Integer, Integer> getContactsTraced() {
    return contactsTraced;
  }

  public Map<Integer, Integer> getPeopleInfected() {
    return peopleInfected;
  }

  public Map<Integer, Integer> getPersonDaysIsolation() {
    return personDaysIsolation;
  }

  public Map<Integer, List<Infection>> getR0Progression() {
    return r0Progression;
  }

  @Override
  public void recordDaysInIsolation(int id, int duration) {
    personDaysIsolation.compute(id, (k, v) -> (v == null) ? duration : v + duration);
  }

  @Override
  public void recordPeopleInfected(int time) {
    peopleInfected.compute(time, (k, v) -> (v == null) ? 1 : ++v);
  }

  @Override
  public void recordContactsTraced(int time, int contactTraced) {
    contactsTraced.compute(time, (k, v) -> v == null ? contactTraced : v + contactTraced);
  }

  @Override
  public void recordInfectionSpread(Case seed, int infections) {
    Infection i = ImmutableInfection.builder().seed(seed.id()).infections(infections).build();

    r0Progression.compute(
        seed.exposedTime(),
        (k, v) ->
            v == null
                ? List.of(i)
                : Stream.of(v, List.of(i))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
  }
}
