package uk.co.ramp.event;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.policy.alert.AlertChecker;

@Service
public class VirusEventProcessor extends CommonVirusEventProcessor<VirusEvent> {
  private final Population population;
  private final AlertChecker alertChecker;

  @Autowired
  public VirusEventProcessor(
      Population population,
      DiseaseProperties diseaseProperties,
      DistributionSampler distributionSampler,
      AlertChecker alertChecker) {
    super(population, diseaseProperties, distributionSampler);
    this.population = population;
    this.alertChecker = alertChecker;
  }

  @Override
  public ProcessedEventResult processEvent(VirusEvent event) {
    // process current event
    Case thisCase = population.get(event.id());
    thisCase.setVirusStatus(thisCase.virusStatus().transitionTo(event.nextStatus()));

    // determine and return next events with time of when they will be processed
    VirusStatus nextStatus = determineNextStatus(event);

    // will return self if at DEAD or RECOVERED
    if (event.nextStatus() != nextStatus) {

      int deltaTime = timeInCompartment(event.nextStatus(), nextStatus);

      VirusEvent subsequentEvent =
          ImmutableVirusEvent.builder()
              .id(event.id())
              .oldStatus(event.nextStatus())
              .nextStatus(nextStatus)
              .time(event.time() + deltaTime)
              .build();

      List<AlertEvent> alertEvents = checkForAlert(event.id(), event.time());

      return ImmutableProcessedEventResult.builder()
          .addNewVirusEvents(subsequentEvent)
          .addAllNewAlertEvents(alertEvents)
          .addNewCompletedVirusEvents(event)
          .build();
    }
    return ImmutableProcessedEventResult.builder().build();
  }

  List<AlertEvent> checkForAlert(int personId, int time) {
    var alertStatus = population.getAlertStatus(personId);
    var virusStatus = population.getVirusStatus(personId);
    return alertChecker
        .checkForAlert(personId, alertStatus, virusStatus, time)
        .collect(Collectors.toList());
  }
}
