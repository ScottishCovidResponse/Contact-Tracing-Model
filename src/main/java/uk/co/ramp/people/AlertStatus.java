package uk.co.ramp.people;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public enum AlertStatus {

    TESTED_POSITIVE,
    TESTED,
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
        validTransitions.put(REQUESTED_TEST, List.of(TESTED));
        //TODO: get clarification here
        validTransitions.put(TESTED, List.of(TESTED_POSITIVE, ALERTED, NONE));
        validTransitions.put(TESTED_POSITIVE, Collections.emptyList());
    }

    public AlertStatus transitionTo(final AlertStatus next) {

        if (!validTransitions.get(this).contains(next)) {
            String message = String.format("It is not valid to transition between statuses %s -> %s", this, next);
            LOGGER.error(message);
            throw new InvalidStatusTransitionException(message);
        }
        return next;
    }

    public List<AlertStatus> getValidTransitions(AlertStatus alertStatus) {
        return validTransitions.get(alertStatus);
    }
}
