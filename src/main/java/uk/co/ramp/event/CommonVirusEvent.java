package uk.co.ramp.event;

import uk.co.ramp.people.VirusStatus;

public interface CommonVirusEvent extends Event {

    int id();

    VirusStatus oldStatus();

    VirusStatus newStatus();

}
