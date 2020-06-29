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
        checkValidTransition(), "Transition from %s to %s is not valid", oldStatus(), nextStatus());
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
