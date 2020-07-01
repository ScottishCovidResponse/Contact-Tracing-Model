package uk.co.ramp.policy.alert;

import static uk.co.ramp.people.AlertStatus.ALERTED;
import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.AlertStatus.REQUESTED_TEST;

import java.util.stream.Stream;
import uk.co.ramp.Population;
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.ImmutableAlertEvent;
import uk.co.ramp.people.VirusStatus;

public class AlertChecker {
  private final TracingPolicy tracingPolicy;
  private final AlertContactTracer alertContactTracer;
  private final Population population;

  AlertChecker(
      TracingPolicy tracingPolicy, AlertContactTracer alertContactTracer, Population population) {
    this.tracingPolicy = tracingPolicy;
    this.alertContactTracer = alertContactTracer;
    this.population = population;
  }

  public Stream<AlertEvent> checkForAlert(
      int personId, VirusStatus nextVirusStatus, int currentTime) {
    var currentReporterAlertStatus = population.getAlertStatus(personId);
    var tracingPolicyReporterVirusStatus = tracingPolicy.reporterVirusStatus();
    var tracingPolicyReporterAlertStatus = tracingPolicy.reporterAlertStatus();

    var requestTestForReporterEvent =
        Stream.of(currentReporterAlertStatus)
            .filter(alertStatus -> alertStatus == NONE)
            .filter(a -> nextVirusStatus == VirusStatus.SYMPTOMATIC)
            .map(
                alertStatus ->
                    ImmutableAlertEvent.builder()
                        .id(personId)
                        .time(currentTime + 1)
                        .oldStatus(NONE)
                        .nextStatus(REQUESTED_TEST)
                        .build());

    var startTime = currentTime - tracingPolicy.recentContactsLookBackTime() + 1;

    var alertsFromRecentContacts =
        alertContactTracer.traceRecentContacts(startTime, currentTime, personId).stream()
            .filter(a -> currentReporterAlertStatus == tracingPolicyReporterAlertStatus)
            .filter(a -> nextVirusStatus == tracingPolicyReporterVirusStatus)
            .map(
                id ->
                    ImmutableAlertEvent.builder()
                        .id(id)
                        .time(currentTime + 1)
                        .oldStatus(NONE)
                        .nextStatus(ALERTED)
                        .build());

    return Stream.concat(requestTestForReporterEvent, alertsFromRecentContacts);
  }
}
