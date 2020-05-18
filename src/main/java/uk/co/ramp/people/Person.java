package uk.co.ramp.people;

import org.immutables.value.Value;

import static uk.co.ramp.people.VirusStatus.*;

@Value.Immutable
public interface Person {

    int id();

    int age();

    Gender gender();

    double compliance();

    double health();

    VirusStatus status();

    int exposedBy();

    int nextStatusChange();

    default boolean isInfectious() {
        return status() == INFECTED || status() == INFECTED_SYMP || status() == EXPOSED_2;
    }
}
