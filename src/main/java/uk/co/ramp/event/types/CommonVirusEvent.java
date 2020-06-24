package uk.co.ramp.event.types;

import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import uk.co.ramp.people.InvalidStatusTransitionException;
import uk.co.ramp.people.VirusStatus;

public interface CommonVirusEvent extends Event {
  int id();

  VirusStatus oldStatus();

  VirusStatus nextStatus();

  @Value.Check
  default void check() {
    Preconditions.checkState(
        checkValidTransition(),
        "Transition from " + oldStatus() + " to " + nextStatus() + " is not valid");
  }

  private boolean checkValidTransition() {
    try {
      oldStatus().transitionTo(nextStatus());
    } catch (InvalidStatusTransitionException ex) {
      return false;
    }
    return true;
  }
}
