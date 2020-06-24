package uk.co.ramp.event.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.InvalidStatusTransitionException;

@Value.Immutable
@JsonSerialize
@JsonDeserialize
public interface AlertEvent extends Event {

  int id();

  AlertStatus nextStatus();

  AlertStatus oldStatus();

  @Value.Check
  default void check() {
    Preconditions.checkState(
        checkValidTransition(), "Transition from oldStatus to nextStatus is not valid");
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
