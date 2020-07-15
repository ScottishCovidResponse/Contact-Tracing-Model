package uk.co.ramp.statistics;

import java.util.List;
import java.util.Map;
import uk.co.ramp.people.Case;
import uk.co.ramp.statistics.types.ImmutableRValueOutput;
import uk.co.ramp.statistics.types.Infection;

public interface StatisticsRecorder {
  void recordDaysInIsolation(int personId, int duration);

  void recordSinglePersonInfected(int time);

  void recordContactsTraced(int time, int numberOfContactTraced);

  void recordInfectionSpread(Case seed, int infections);

  List<ImmutableRValueOutput> getRollingAverage(int period);

  Map<Integer, Integer> getContactsTraced();

  Map<Integer, Integer> getPeopleInfected();

  Map<Integer, Integer> getPersonDaysIsolation();

  Map<Integer, List<Infection>> getR0Progression();
}
