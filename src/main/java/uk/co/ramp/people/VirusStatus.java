package uk.co.ramp.people;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

public enum VirusStatus {

    DEAD(-1, Collections.emptyList()),
    RECOVERED(1, Collections.emptyList()),
    SEVERELY_SYMPTOMATIC(6, List.of(RECOVERED, DEAD)),
    SYMPTOMATIC(5, List.of(RECOVERED, SEVERELY_SYMPTOMATIC)),
    ASYMPTOMATIC(4, List.of(RECOVERED)),
    PRESYMPTOMATIC(3, List.of(SYMPTOMATIC)),
    EXPOSED(2, List.of(PRESYMPTOMATIC, ASYMPTOMATIC)),
    SUSCEPTIBLE(0, List.of(EXPOSED));

    private final int val;
    private static final Logger LOGGER = LogManager.getLogger(VirusStatus.class);
    private final List<VirusStatus> validTransitions;

    VirusStatus(int i, final List<VirusStatus> validTransitions) {
        this.val = i;
        this.validTransitions = validTransitions;
    }

    public int getVal() {
        return val;
    }

    public List<VirusStatus> getValidTransitions() {
        return validTransitions;
    }

    public VirusStatus transitionTo(final VirusStatus next) {

        if (!validTransitions.contains(next)) {
            String message = String.format("It is not valid to transition between statuses %s -> %s", this, next);
            LOGGER.error(message);
            throw new InvalidStatusTransitionException(message);
        }
        return next;
    }

}
