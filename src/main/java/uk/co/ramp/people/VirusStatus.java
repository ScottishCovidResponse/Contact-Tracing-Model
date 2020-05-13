package uk.co.ramp.people;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Set;

public enum VirusStatus {

    DEAD(-1, Collections.emptySet()),
    RECOVERED(1, Collections.emptySet()),
    INFECTED_SYMP(5, Set.of(RECOVERED, DEAD)),
    INFECTED(4, Set.of(RECOVERED)),
    EXPOSED_2(3, Set.of(INFECTED, INFECTED_SYMP)),
    // TODO remove infected from here
    EXPOSED(2, Set.of(EXPOSED_2)),
    SUSCEPTIBLE(0, Set.of(EXPOSED));

    private final int val;
    private static final Logger LOGGER = LogManager.getLogger(VirusStatus.class);
    private final Set<VirusStatus> validTransitions;

    VirusStatus(int i, final Set<VirusStatus> validTransitions) {
        this.val = i;
        this.validTransitions = validTransitions;
    }

    public int getVal() {
        return val;
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
