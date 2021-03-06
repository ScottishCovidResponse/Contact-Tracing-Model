package uk.co.ramp.io;

import java.util.Map;
import uk.co.ramp.people.VirusStatus;

public class InfectionRates {

  private final Map<VirusStatus, Double> infectionRates;

  public InfectionRates(Map<VirusStatus, Double> type) {
    infectionRates = type;
  }

  public double getInfectionRate(VirusStatus status) {
    return infectionRates.getOrDefault(status, 0d);
  }

  public Map<VirusStatus, Double> getInfectionRates() {
    return infectionRates;
  }
}
