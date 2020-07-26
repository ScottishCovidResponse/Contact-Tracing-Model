package uk.co.ramp.event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.ImmutableProcessedEventResult;
import uk.co.ramp.event.types.ImmutableVirusEvent;
import uk.co.ramp.event.types.ProcessedEventResult;
import uk.co.ramp.event.types.VirusEvent;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.policy.alert.AlertChecker;

@Service
public class VirusEventProcessor extends CommonVirusEventProcessor<VirusEvent> {
  private final Population population;
  private final AlertChecker alertChecker;
  private final DistributionSampler distributionSampler;
  private static final Logger LOGGER = LogManager.getLogger(VirusEventProcessor.class);

  @Autowired
  public VirusEventProcessor(
      Population population,
      StandardProperties properties,
      DiseaseProperties diseaseProperties,
      DistributionSampler distributionSampler,
      AlertChecker alertChecker) {
    super(population, properties, diseaseProperties, distributionSampler);
    this.population = population;
    this.alertChecker = alertChecker;
    this.distributionSampler = distributionSampler;
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

      List<AlertEvent> alertEvents;
      if (thisCase.reportingCompliance() > distributionSampler.uniformBetweenZeroAndOne()) {
        alertEvents = checkForAlert(event);
      } else {
        LOGGER.debug("Person with id: {} is not complying with infection reporting", thisCase.id());
        alertEvents = new ArrayList<>();
      }
      return ImmutableProcessedEventResult.builder()
          .addNewVirusEvents(subsequentEvent)
          .addAllNewAlertEvents(alertEvents)
          .addNewCompletedVirusEvents(event)
          .build();
    }
    return ImmutableProcessedEventResult.builder().build();
  }

  List<AlertEvent> checkForAlert(VirusEvent event) {
    var alertStatus = population.getAlertStatus(event.id());
    var virusStatus = event.nextStatus();
    return alertChecker
        .checkForAlert(event.id(), alertStatus, virusStatus, event.time())
        .collect(Collectors.toList());
  }
}
