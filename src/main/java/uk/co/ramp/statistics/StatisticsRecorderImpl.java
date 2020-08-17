package uk.co.ramp.statistics;

import java.util.*;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.Case;
import uk.co.ramp.statistics.types.ImmutableInfection;
import uk.co.ramp.statistics.types.ImmutableRValueOutput;
import uk.co.ramp.statistics.types.Infection;

public class StatisticsRecorderImpl implements StatisticsRecorder {

  private final Map<Integer, Integer> personDaysIsolation = new HashMap<>();
  private final Map<Integer, Integer> peopleInfected = new HashMap<>();
  private final Map<Integer, Integer> contactsTraced = new HashMap<>();
  private final Map<Integer, Integer> testsConducted = new HashMap<>();
  private final Map<Integer, List<Integer>> delayedTests = new HashMap<>();
  private final Map<Integer, List<Infection>> r0Progression = new HashMap<>();
  private final Map<AlertStatus, Integer> incorrectTests = new EnumMap<>(AlertStatus.class);

  private final StandardProperties properties;

  public StatisticsRecorderImpl(StandardProperties properties) {
    this.properties = properties;
  }

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
  public Map<Integer, Integer> getTestsConducted() {
    return testsConducted;
  }

  @Override
  public Map<Integer, List<Integer>> getDelayedTests() {
    return delayedTests;
  }

  @Override
  public int getTestsConducted(int time) {
    return testsConducted.getOrDefault(time, 0);
  }

  @Override
  public int getFalseNegatives() {
    return incorrectTests.get(AlertStatus.TESTED_NEGATIVE);
  }

  @Override
  public int getFalsePositives() {
    return incorrectTests.get(AlertStatus.TESTED_POSITIVE);
  }

  public void recordDaysInIsolation(int personId, int duration) {
    personDaysIsolation.compute(personId, (k, v) -> (v == null) ? duration : v + duration);
  }

  public void recordSinglePersonInfected(int time) {
    peopleInfected.compute(time, (k, v) -> (v == null) ? 1 : ++v);
  }

  public void recordContactsTraced(int time, int numberOfContactTraced) {
    contactsTraced.compute(
        time, (k, v) -> v == null ? numberOfContactTraced : v + numberOfContactTraced);
  }

  public void recordInfectionSpread(Case seed, int infections) {
    Infection i = ImmutableInfection.builder().seed(seed.id()).infections(infections).build();
    r0Progression.computeIfAbsent(seed.exposedTime(), k -> new ArrayList<>()).add(i);
  }

  @Override
  public void recordIncorrectTestResult(AlertStatus alertStatus) {
    incorrectTests.compute(alertStatus, (k, v) -> (v == null) ? 1 : ++v);
  }

  @Override
  public void recordTestConducted(int time) {
    testsConducted.compute(time, (k, v) -> (v == null) ? 1 : ++v);
  }

  @Override
  public void recordTestDelayed(int time, int id) {
    delayedTests.computeIfAbsent(time, k -> new ArrayList<>()).add(id);
  }

  public List<ImmutableRValueOutput> getRollingAverage(int period) {
    MovingAverage movingAverage = new MovingAverage(period);
    Map<Integer, List<Infection>> inf = getR0Progression();
    List<ImmutableRValueOutput> rValueOutputs = new ArrayList<>();
    for (int i = 0; i < properties.timeLimit(); i++) {
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
    return rValueOutputs;
  }
}
