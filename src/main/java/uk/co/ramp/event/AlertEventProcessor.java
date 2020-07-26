package uk.co.ramp.event;

import static uk.co.ramp.people.AlertStatus.*;

import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.Distribution;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.distribution.ImmutableDistribution;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.statistics.StatisticsRecorder;
import uk.co.ramp.utilities.ImmutableMeanMax;
import uk.co.ramp.utilities.MeanMax;

public class AlertEventProcessor implements EventProcessor<AlertEvent> {
  private static final Logger LOGGER = LogManager.getLogger(AlertEventProcessor.class);

  private final Population population;
  private final DiseaseProperties diseaseProperties;
  private final DistributionSampler distributionSampler;
  private final StandardProperties properties;
  private final StatisticsRecorder statisticsRecorder;

  public AlertEventProcessor(
      Population population,
      StandardProperties properties,
      DiseaseProperties diseaseProperties,
      DistributionSampler distributionSampler,
      StatisticsRecorder statisticsRecorder) {
    this.population = population;
    this.diseaseProperties = diseaseProperties;
    this.distributionSampler = distributionSampler;
    this.properties = properties;
    this.statisticsRecorder = statisticsRecorder;
  }

  @Override
  public ProcessedEventResult processEvent(AlertEvent event) {
    if (population.getAlertStatus(event.id()) != event.oldStatus()) {
      return ImmutableProcessedEventResult.builder().build();
    }

    population.setAlertStatus(event.id(), event.nextStatus());

    Optional<AlertStatus> proposedStatus = determineNextAlertStatus(event);
    Optional<AlertStatus> nextStatus =
        proposedStatus.map(ps -> event.nextStatus().transitionTo(ps));

    if (nextStatus.isEmpty()) {
      return ImmutableProcessedEventResult.builder().addNewCompletedAlertEvents(event).build();
    }

    if (nextStatus.get() != event.nextStatus()) {
      int deltaTime = timeInStatus(nextStatus.get());

      AlertEvent subsequentEvent =
          ImmutableAlertEvent.builder()
              .id(event.id())
              .oldStatus(event.nextStatus())
              .nextStatus(nextStatus.get())
              .time(event.time() + deltaTime)
              .build();

      return ImmutableProcessedEventResult.builder()
          .addNewAlertEvents(subsequentEvent)
          .addNewCompletedAlertEvents(event)
          .build();
    }
    return ImmutableProcessedEventResult.builder().build();
  }

  private Optional<AlertStatus> determineNextAlertStatus(AlertEvent event) {
    List<AlertStatus> newStatus = getValidTransitions(event.nextStatus());

    if (newStatus.isEmpty()) return Optional.empty();
    if (newStatus.size() == 1) return Optional.of(newStatus.get(0));

    switch (event.nextStatus()) {
      case AWAITING_RESULT:
        return determineTestResult(population.isInfectious(event.id()));
      case NONE:
        return Optional.of(NONE);
      default:
        LOGGER.error(event);
        throw new EventException("There is no case for the event" + event);
    }
  }

  Optional<AlertStatus> determineTestResult(boolean isInfectious) {

    double testEffectiveness = distributionSampler.uniformBetweenZeroAndOne();

    if (isInfectious) {
      if (testEffectiveness < diseaseProperties.testPositiveAccuracy()) {
        statisticsRecorder.recordCorrectTestResult(TESTED_POSITIVE);
        return Optional.of(TESTED_POSITIVE);
      } else {
        statisticsRecorder.recordIncorrectTestResult(TESTED_NEGATIVE);
        return Optional.of(TESTED_NEGATIVE);
      }
    } else {
      if (testEffectiveness < diseaseProperties.testNegativeAccuracy()) {
        statisticsRecorder.recordCorrectTestResult(TESTED_NEGATIVE);
        return Optional.of(TESTED_NEGATIVE);
      } else {
        statisticsRecorder.recordIncorrectTestResult(TESTED_POSITIVE);
        return Optional.of(TESTED_POSITIVE);
      }
    }
  }

  int timeInStatus(AlertStatus newStatus) {

    MeanMax progressionData;
    switch (newStatus) {
      case TESTED_POSITIVE:
      case TESTED_NEGATIVE:
      case ALERTED:
        progressionData = ImmutableMeanMax.builder().mean(1).max(1).build();
        break;
      case AWAITING_RESULT:
        progressionData = diseaseProperties.timeTestResult();
        break;
      case REQUESTED_TEST:
        progressionData = diseaseProperties.timeTestAdministered();
        break;
      case NONE:
        return 0;
      default:
        throw new IllegalStateException("Should not reach this state");
    }

    Distribution distribution =
        ImmutableDistribution.builder()
            .type(diseaseProperties.progressionDistribution())
            .mean(progressionData.mean())
            .max(progressionData.max())
            .build();
    return distributionSampler.getDistributionValue(distribution) * properties.timeStepsPerDay();
  }
}
