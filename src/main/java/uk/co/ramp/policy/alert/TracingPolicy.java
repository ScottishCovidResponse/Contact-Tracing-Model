package uk.co.ramp.policy.alert;

import java.util.List;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;
import uk.co.ramp.distribution.Distribution;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.VirusStatus;

@TypeAdapters
@Immutable
interface TracingPolicy {
  @TypeAdapters
  @Immutable
  interface TracingPolicyItem {
    AlertStatus reporterAlertStatus();

    VirusStatus reporterVirusStatus();

    int recentContactsLookBackTime();

    Distribution timeDelayPerTraceLink();

    Distribution probabilitySkippingTraceLink();
  }

  String description();

  List<TracingPolicyItem> policies();

  int noOfTracingLevels();

  Distribution probabilitySkippingTraceLinkThreshold();

  // TODO add Precondition checks to ensure each (reporterAlertStatus and reporterVirusStatus)
  // pairs are unique across all policy items.
}
