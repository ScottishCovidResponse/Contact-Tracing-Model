package uk.co.ramp.policy.alert;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;
import uk.co.ramp.distribution.BoundedDistribution;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.VirusStatus;

@JsonSerialize
@JsonDeserialize
@Immutable
interface TracingPolicy {
  @TypeAdapters
  @Immutable
  interface TracingPolicyItem {
    AlertStatus reporterAlertStatus();

    VirusStatus reporterVirusStatus();

    int recentContactsLookBackTime();

    BoundedDistribution timeDelayPerTraceLink();

    BoundedDistribution probabilitySkippingTraceLink();
  }

  String description();

  List<ImmutableTracingPolicyItem> policies();

  int noOfTracingLevels();

  BoundedDistribution probabilitySkippingTraceLinkThreshold();

  // TODO add Precondition checks to ensure each (reporterAlertStatus and reporterVirusStatus)
  // pairs are unique across all policy items.
}
