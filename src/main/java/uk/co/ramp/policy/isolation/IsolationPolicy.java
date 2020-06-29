package uk.co.ramp.policy.isolation;

import uk.co.ramp.people.Case;

public interface IsolationPolicy {
  boolean isContactIsolated(
      Case caseA,
      Case caseB,
      double contactWeight,
      double actualInfectedProportion,
      int currentTime);
}
