package uk.co.ramp.io.types;

import static org.immutables.value.Value.Check;

import com.google.common.base.Preconditions;
import java.util.Map;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;
import uk.co.ramp.utilities.MinMax;

@TypeAdapters
@Immutable
public interface PopulationProperties {
  Map<Integer, Double> populationDistribution();

  Map<Integer, MinMax> populationAges();

  double genderBalance();

  @Check
  default void check() {
    final double eps = 1e-6;
    Preconditions.checkState(
        Math.abs(populationDistribution().values().stream().mapToDouble(d -> d).sum() - 1.0) < eps,
        "Sum of population distribution should be 1");
    Preconditions.checkState(
        populationDistribution().keySet().equals(populationAges().keySet()),
        "Bins should be consistent");
    Preconditions.checkState(
        genderBalance() >= 0 && genderBalance() <= 1, "Gender balance should be between 0 and 1");
  }
}
