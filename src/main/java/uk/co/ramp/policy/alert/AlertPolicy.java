package uk.co.ramp.policy.alert;

import org.immutables.value.Value.Immutable;

@Immutable
interface AlertPolicy {
  int recentContactsLookBackTime();
}
