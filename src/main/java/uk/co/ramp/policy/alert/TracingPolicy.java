package uk.co.ramp.policy.alert;

import org.immutables.value.Value.Immutable;
import uk.co.ramp.distribution.Distribution;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.VirusStatus;

@Immutable
interface TracingPolicy {
  String description();

  AlertStatus reporterAlertStatus();

  VirusStatus reporterVirusStatus();

  int recentContactsLookBackTime();

  int noOfTracingLevels();

  Distribution delayPerTraceLink();
}
