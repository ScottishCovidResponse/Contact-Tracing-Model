package uk.co.ramp.policy;

import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;
import uk.co.ramp.distribution.Distribution;

import java.util.Optional;

@Immutable
@TypeAdapters
interface IsolationProperty {
    String id();
    Distribution isolationProbabilityDistribution();
    Optional<Distribution> isolationTimeDistribution();
    int priority();
}