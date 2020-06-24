package uk.co.ramp.event;

import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;

@Service
public class InfectionEventProcessor extends CommonVirusEventProcessor<InfectionEvent> {
  private final Population population;

  @Autowired
  public InfectionEventProcessor(
      Population population,
      DiseaseProperties diseaseProperties,
      DistributionSampler distributionSampler) {
    super(population, diseaseProperties, distributionSampler);
    this.population = population;
  }

  @Override
  public ProcessedEventResult processEvent(InfectionEvent event) {
    Case thisCase = population.get(event.id());
    if (thisCase.virusStatus() == SUSCEPTIBLE) {
      thisCase.setVirusStatus(thisCase.virusStatus().transitionTo(event.nextStatus()));
      thisCase.setExposedBy(event.exposedBy());
      thisCase.setExposedTime(event.exposedTime());

      VirusStatus nextStatus = determineNextStatus(event);
      int deltaTime = timeInCompartment(event.nextStatus(), nextStatus);

      VirusEvent subsequentEvent =
          ImmutableVirusEvent.builder()
              .id(event.id())
              .oldStatus(event.nextStatus())
              .nextStatus(nextStatus)
              .time(event.time() + deltaTime)
              .build();

      return ImmutableProcessedEventResult.builder()
          .addNewVirusEvents(subsequentEvent)
          .addCompletedEvents(event)
          .build();
    }
    return ImmutableProcessedEventResult.builder().build();
  }
}
