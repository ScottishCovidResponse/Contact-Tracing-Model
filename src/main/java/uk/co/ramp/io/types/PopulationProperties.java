package uk.co.ramp.io.types;

import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;
import uk.co.ramp.utilities.MinMax;

import java.util.Map;

@TypeAdapters
@Immutable
public interface PopulationProperties {
    Map<Integer, Double> populationDistribution();
    Map<Integer, MinMax> populationAges();
    double genderBalance();
}
