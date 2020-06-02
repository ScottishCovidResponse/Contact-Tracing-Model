package uk.co.ramp.policy;

import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;
import java.util.Map;

@TypeAdapters
@Immutable
interface IsolationProperties {
    Map<String, ProportionInfectedIsolationProperty> globalIsolationPolicies();
    IndividualIsolationPolicies individualIsolationPolicies();
    IsolationProperty defaultPolicy();
    int isolationProbabilityDistributionThreshold();
}
