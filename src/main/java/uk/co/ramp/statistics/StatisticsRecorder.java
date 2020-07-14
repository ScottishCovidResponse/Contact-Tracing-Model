package uk.co.ramp.statistics;

import java.util.List;
import java.util.Map;
import uk.co.ramp.people.Case;
import uk.co.ramp.statistics.types.Infection;

public interface StatisticsRecorder {
  void recordDaysInIsolation(int id, int duration);

  void recordPeopleInfected(int time);

  void recordContactsTraced(int time, int contactTraced);

  void recordInfectionSpread(Case seed, int infections);

  Map<Integer, Integer> getContactsTraced();

  Map<Integer, Integer> getPeopleInfected();

  Map<Integer, Integer> getPersonDaysIsolation();

  Map<Integer, List<Infection>> getR0Progression();
}
