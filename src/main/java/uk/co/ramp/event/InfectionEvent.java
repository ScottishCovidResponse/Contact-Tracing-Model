package uk.co.ramp.event;

import org.immutables.value.Value;

@Value.Immutable
public interface InfectionEvent extends CommonVirusEvent {

    int exposedBy();

    int exposedTime();
}
