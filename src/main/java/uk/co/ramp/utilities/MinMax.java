package uk.co.ramp.utilities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@JsonSerialize
@JsonDeserialize
@Immutable
public abstract class MinMax {
  public abstract int min();

  public abstract int max();

  public static ImmutableMinMax of(int a, int b) {
    return ImmutableMinMax.builder().min(Math.min(a, b)).max(Math.max(a, b)).build();
  }

  @Value.Check
  protected void check() {
    Preconditions.checkState(min() <= max(), "Min should be less than or equal to max");
  }
}
