package uk.co.ramp.io.types;

import static org.immutables.value.Value.Check;

import com.google.common.base.Preconditions;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;
import uk.ramp.distribution.Distribution;

@TypeAdapters
@Immutable
public interface PopulationProperties {
  Distribution distribution();

  double genderBalance();

  @Check
  default void check() {
    Preconditions.checkState(
        genderBalance() >= 0 && genderBalance() <= 1, "Gender balance should be between 0 and 1");
  }
}
