package uk.co.ramp.io.types;

import com.google.common.base.Preconditions;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;
import uk.co.ramp.utilities.MinMax;

import java.util.Map;

import static org.immutables.value.Value.Check;

@TypeAdapters
@Immutable
public interface PopulationProperties {
    Map<Integer, Double> populationDistribution();
    Map<Integer, MinMax> populationAges();
    double genderBalance();

    @Check
    default void check() {
        Preconditions.checkState(populationDistribution().values().stream().mapToDouble(d -> d).sum() == 1, "Sum of population distribution should be 1");
        Preconditions.checkState(populationDistribution().keySet().equals(populationAges().keySet()), "Bins should be consistent");
        Preconditions.checkState(genderBalance() >= 0 && genderBalance() <= 1, "Gender balance should be between 0 and 1");
    }
}
