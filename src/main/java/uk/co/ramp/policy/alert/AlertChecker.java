package uk.co.ramp.policy.alert;

import static uk.co.ramp.people.AlertStatus.ALERTED;
import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.AlertStatus.REQUESTED_TEST;
import static uk.co.ramp.people.VirusStatus.SYMPTOMATIC;

import java.util.stream.Stream;
import uk.co.ramp.Population;
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.ImmutableAlertEvent;
import uk.co.ramp.people.VirusStatus;

public class AlertChecker {
  private final AlertPolicy alertPolicy;
  private final AlertContactTracer alertContactTracer;
  private final Population population;

  AlertChecker(
      AlertPolicy alertPolicy, AlertContactTracer alertContactTracer, Population population) {
    this.alertPolicy = alertPolicy;
    this.alertContactTracer = alertContactTracer;
    this.population = population;
  }

  public Stream<AlertEvent> checkForAlert(
      int personId, VirusStatus nextVirusStatus, int currentTime) {
    if (nextVirusStatus == SYMPTOMATIC && population.getAlertStatus(personId) == NONE) {
      AlertEvent requestTestForReporterEvent =
          ImmutableAlertEvent.builder()
              .id(personId)
              .time(currentTime + 1)
              .oldStatus(NONE)
              .nextStatus(REQUESTED_TEST)
              .build();

      var startTime = currentTime - alertPolicy.recentContactsLookBackTime() + 1;

      Stream<AlertEvent> alertsFromRecentContacts =
          alertContactTracer.traceRecentContacts(startTime, currentTime, personId).stream()
              .map(
                  id ->
                      ImmutableAlertEvent.builder()
                          .id(id)
                          .time(currentTime + 1)
                          .oldStatus(NONE)
                          .nextStatus(ALERTED)
                          .build());
      return Stream.concat(Stream.of(requestTestForReporterEvent), alertsFromRecentContacts);
    }
    return Stream.empty();
  }
}
