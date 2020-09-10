package uk.co.ramp.io.types;

import java.util.List;
import java.util.OptionalDouble;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import uk.co.ramp.utilities.ImmutableMinMax;

@Value.Immutable
@Gson.TypeAdapters
public interface PopulationOverrides {

  List<ImmutableAgeDependentHealth> ageDependentList();

  OptionalDouble fixedIsolationCompliance();

  OptionalDouble fixedReportingCompliance();

  @Gson.TypeAdapters
  @Value.Immutable
  interface AgeDependentHealth {
    ImmutableMinMax range();

    double modifier();
  }
}
