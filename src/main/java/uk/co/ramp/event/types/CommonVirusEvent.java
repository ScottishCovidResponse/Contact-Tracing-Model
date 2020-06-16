package uk.co.ramp.event.types;

import uk.co.ramp.people.VirusStatus;

public interface CommonVirusEvent extends Event {
    int id();

    VirusStatus oldStatus();

    VirusStatus nextStatus();

}
