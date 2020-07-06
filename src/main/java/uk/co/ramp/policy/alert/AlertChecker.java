package uk.co.ramp.policy.alert;

import static uk.co.ramp.people.AlertStatus.ALERTED;
import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.AlertStatus.REQUESTED_TEST;

import java.util.Optional;
import java.util.stream.Stream;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.ImmutableAlertEvent;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.policy.alert.TracingPolicy.TracingPolicyItem;

public class AlertChecker {
  private final TracingPolicy tracingPolicy;
  private final AlertContactTracer alertContactTracer;
  private final DistributionSampler distributionSampler;

  AlertChecker(
      TracingPolicy tracingPolicy,
      AlertContactTracer alertContactTracer,
      DistributionSampler distributionSampler) {
    this.tracingPolicy = tracingPolicy;
    this.alertContactTracer = alertContactTracer;
    this.distributionSampler = distributionSampler;
  }

  private Optional<TracingPolicyItem> findPolicyItem(
      VirusStatus virusStatusKey, AlertStatus alertStatusKey) {
    return tracingPolicy.policies().stream()
        .filter(p -> p.reporterVirusStatus() == virusStatusKey)
        .filter(p -> p.reporterAlertStatus() == alertStatusKey)
        .findAny();
  }

  public Stream<AlertEvent> checkForAlert(
      int personId,
      AlertStatus currentReporterAlertStatus,
      VirusStatus currentReporterVirusStatus,
      int currentTime) {
    Stream<AlertEvent> requestTestForReporterEvent =
        Stream.of(currentReporterAlertStatus)
            .filter(alertStatus -> alertStatus == NONE)
            .filter(
                a ->
                    currentReporterVirusStatus == VirusStatus.SYMPTOMATIC
                        || currentReporterVirusStatus == VirusStatus.SEVERELY_SYMPTOMATIC)
            .map(
                alertStatus ->
                    ImmutableAlertEvent.builder()
                        .id(personId)
                        .time(currentTime + 1)
                        .oldStatus(NONE)
                        .nextStatus(REQUESTED_TEST)
                        .build());

    var tracingPolicyItem = findPolicyItem(currentReporterVirusStatus, currentReporterAlertStatus);

    if (tracingPolicyItem.map(policy -> !shouldPerformTracingForThisLink(policy)).orElse(true)) {
      return requestTestForReporterEvent;
    }
    var startTime = currentTime - tracingPolicyItem.get().recentContactsLookBackTime() + 1;

    var alertsFromRecentContacts =
        alertContactTracer.traceRecentContacts(startTime, currentTime, personId).stream()
            .map(
                id ->
                    ImmutableAlertEvent.builder()
                        .id(id)
                        .time(
                            currentTime
                                + distributionSampler.getDistributionValue(
                                    tracingPolicyItem.get().timeDelayPerTraceLink()))
                        .oldStatus(NONE)
                        .nextStatus(ALERTED)
                        .build());

    return Stream.concat(requestTestForReporterEvent, alertsFromRecentContacts);
  }

  private boolean shouldPerformTracingForThisLink(TracingPolicyItem tracingPolicyItem) {
    var thresholdDistribution = tracingPolicy.probabilitySkippingTraceLinkThreshold();
    var thresholdVal = distributionSampler.getDistributionValue(thresholdDistribution);
    var skipTracingDistribution = tracingPolicyItem.probabilitySkippingTraceLink();
    var skipTracingVal = distributionSampler.getDistributionValue(skipTracingDistribution);
    return thresholdVal > skipTracingVal;
  }
}
