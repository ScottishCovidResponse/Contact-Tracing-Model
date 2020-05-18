package uk.co.ramp.people;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum AlertStatus {
    TESTED_POSITIVE,
    TESTED,
    REQUESTED_TEST,
    ALERTED,
    NONE;

    private static final Map<AlertStatus, List<AlertStatus>> validTransitions;

    // Loaded as static to avoid forward reference error
    static {
        validTransitions = new HashMap<>();
        validTransitions.put(NONE, List.of(ALERTED, REQUESTED_TEST));
        validTransitions.put(ALERTED, List.of(REQUESTED_TEST));
        validTransitions.put(REQUESTED_TEST, List.of(TESTED));
        validTransitions.put(TESTED, List.of(TESTED_POSITIVE, ALERTED));
        validTransitions.put(TESTED_POSITIVE, Collections.emptyList());
    }


    public List<AlertStatus> getValidTransitions(AlertStatus alertStatus) {
        return validTransitions.get(alertStatus);
    }
}
