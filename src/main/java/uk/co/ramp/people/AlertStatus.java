package uk.co.ramp.people;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum AlertStatus {
  TESTED_POSITIVE,
  TESTED_NEGATIVE,
  AWAITING_RESULT,
  REQUESTED_TEST,
  ALERTED,
  NONE;

  private static final Logger LOGGER = LogManager.getLogger(AlertStatus.class);
  private static final Map<AlertStatus, List<AlertStatus>> validTransitions;

  // Loaded as static to avoid forward reference error
  static {
    validTransitions = new EnumMap<>(AlertStatus.class);
    validTransitions.put(NONE, List.of(ALERTED, REQUESTED_TEST));
    validTransitions.put(ALERTED, List.of(REQUESTED_TEST));
    validTransitions.put(REQUESTED_TEST, List.of(AWAITING_RESULT));
    // TODO: get clarification here
    validTransitions.put(AWAITING_RESULT, List.of(TESTED_POSITIVE, TESTED_NEGATIVE));
    validTransitions.put(TESTED_NEGATIVE, List.of(NONE));
    validTransitions.put(TESTED_POSITIVE, Collections.emptyList());
  }

  public AlertStatus transitionTo(final AlertStatus next) {

    if (!validTransitions.get(this).contains(next)) {
      String message =
          String.format("It is not valid to transition between statuses %s -> %s", this, next);
      LOGGER.error(message);
      throw new InvalidStatusTransitionException(message);
    }
    return next;
  }

  public static List<AlertStatus> getValidTransitions(AlertStatus alert) {
    return validTransitions.get(alert);
  }
}
