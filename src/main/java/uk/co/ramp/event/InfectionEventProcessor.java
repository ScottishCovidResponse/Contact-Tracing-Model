package uk.co.ramp.event;

import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.statistics.StatisticsRecorder;

@Service
public class InfectionEventProcessor extends CommonVirusEventProcessor<InfectionEvent> {
  private final Population population;
  private final StatisticsRecorder statisticsRecorder;

  @Autowired
  public InfectionEventProcessor(
      Population population,
      StandardProperties properties,
      DiseaseProperties diseaseProperties,
      DistributionSampler distributionSampler,
      StatisticsRecorder statisticsRecorder) {
    super(population, properties, diseaseProperties, distributionSampler);
    this.population = population;
    this.statisticsRecorder = statisticsRecorder;
  }

  @Override
  public ProcessedEventResult processEvent(InfectionEvent event) {
    if (population.getVirusStatus(event.id()) == SUSCEPTIBLE) {
      population.setVirusStatus(event.id(), event.nextStatus());
      population.setExposedBy(event.id(), event.exposedBy());
      population.setExposedTime(event.id(), event.exposedTime());

      VirusStatus nextStatus = determineNextStatus(event);
      int deltaTime = timeInCompartment(event.nextStatus(), nextStatus);

      statisticsRecorder.recordPeopleInfected(event.exposedTime());

      VirusEvent subsequentEvent =
          ImmutableVirusEvent.builder()
              .id(event.id())
              .oldStatus(event.nextStatus())
              .nextStatus(nextStatus)
              .time(event.time() + deltaTime)
              .build();

      return ImmutableProcessedEventResult.builder()
          .addNewVirusEvents(subsequentEvent)
          .addNewCompletedInfectionEvents(event)
          .build();
    }
    return ImmutableProcessedEventResult.builder().build();
  }
}
