package uk.co.ramp.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.Case;
import uk.co.ramp.statistics.types.ImmutableInfection;
import uk.co.ramp.statistics.types.ImmutableRValueOutput;
import uk.co.ramp.statistics.types.Infection;

public class StatisticsRecorderImpl implements StatisticsRecorder {

  private final Map<Integer, Integer> personDaysIsolation;
  private final Map<Integer, Integer> peopleInfected;
  private final Map<Integer, Integer> contactsTraced;
  private final Map<Integer, List<Infection>> r0Progression;
  private final Map<AlertStatus, Integer> incorrectTests;
  private final Map<AlertStatus, Integer> correctTests;

  private final StandardProperties properties;

  public StatisticsRecorderImpl(
      StandardProperties properties,
      Map<Integer, Integer> personDaysIsolation,
      Map<Integer, Integer> peopleInfected,
      Map<Integer, Integer> contactsTraced,
      Map<Integer, List<Infection>> r0Progression,
      Map<AlertStatus, Integer> incorrectTests,
      Map<AlertStatus, Integer> correctTests) {
    this.properties = properties;

    this.personDaysIsolation = personDaysIsolation;
    this.peopleInfected = peopleInfected;
    this.contactsTraced = contactsTraced;
    this.r0Progression = r0Progression;
    this.incorrectTests = incorrectTests;
    this.correctTests = correctTests;
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
  public int getFalseNegatives() {
    return incorrectTests.getOrDefault(AlertStatus.TESTED_NEGATIVE, 0);
  }

  @Override
  public int getFalsePositives() {
    return incorrectTests.getOrDefault(AlertStatus.TESTED_POSITIVE, 0);
  }

  @Override
  public int getTruePositives() {
    return correctTests.getOrDefault(AlertStatus.TESTED_POSITIVE, 0);
  }

  @Override
  public int getTrueNegatives() {
    return correctTests.getOrDefault(AlertStatus.TESTED_NEGATIVE, 0);
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
  public void recordCorrectTestResult(AlertStatus alertStatus) {
    correctTests.compute(alertStatus, (k, v) -> (v == null) ? 1 : ++v);
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
