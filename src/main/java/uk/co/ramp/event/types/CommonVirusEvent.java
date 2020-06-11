package uk.co.ramp.event.types;

import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;

public interface CommonVirusEvent extends Event {

    void applyEventToCase(Case aCase);

    int id();

    VirusStatus oldStatus();

    VirusStatus nextStatus();

}
