package uk.co.ramp.policy;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.util.Map;

@Gson.TypeAdapters
@Value.Immutable
interface IndividualIsolationPolicies {
    Map<String, VirusStatusIsolationProperty> virusStatusPolicies();
    Map<String, AlertStatusIsolationProperty> alertStatusPolicies();
}