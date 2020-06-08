package uk.co.ramp.policy;

import org.immutables.gson.Gson;
import org.immutables.value.Value;
import uk.co.ramp.people.AlertStatus;

@Gson.TypeAdapters
@Value.Immutable
interface AlertStatusIsolationProperty {
    AlertStatus alertStatus();
    IsolationProperty isolationProperty();
}