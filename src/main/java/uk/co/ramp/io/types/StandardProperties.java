package uk.co.ramp.io.types;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.OptionalInt;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;

@TypeAdapters
@Immutable
public interface StandardProperties {
  int populationSize();

  int timeLimitDays();

  int timeStepsPerDay();

  double[] timeStepSpread();

  int initialExposures();

  OptionalInt seed();

  OptionalInt dayOffset();

  boolean steadyState();

  @Check
  default void check() {
    Preconditions.checkState(populationSize() > 0, "Population size should be greater than 0");
    Preconditions.checkState(timeLimitDays() >= 0, "Time limit should not be negative");
    Preconditions.checkState(initialExposures() >= 0, "Initial exposures should not be negative");
    Preconditions.checkState(
        timeStepsPerDay() > 0, "There should be at least one time step per day");
    Preconditions.checkState(
        timeStepSpread().length == timeStepsPerDay(),
        "The spread of events should be the same dimension as the number of time steps per day");
    Preconditions.checkState(
        Arrays.stream(timeStepSpread()).sum() == 1d, "The time step spread should equal 1.0");
  }
}
