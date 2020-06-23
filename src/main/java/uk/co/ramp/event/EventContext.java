package uk.co.ramp.event;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.InitialCaseReader;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.policy.IsolationPolicy;

@SpringBootConfiguration
public class EventContext {
  private final EventList<Event> completedEventList = new EventList<>();

  @Bean
  public EventListGroup eventListGroup() {
    EventList<AlertEvent> alertEventList = new EventList<>();
    EventList<ContactEvent> contactEventList = new EventList<>();
    EventList<InfectionEvent> infectionEventList = new EventList<>();
    EventList<VirusEvent> virusEventList = new EventList<>();
    return new EventListGroup(
        alertEventList, contactEventList, infectionEventList, virusEventList, completedEventList);
  }

  @Bean
  public EventRunner eventRunner(
      Population population,
      DiseaseProperties diseaseProperties,
      DistributionSampler distributionSampler,
      IsolationPolicy isolationPolicy,
      InitialCaseReader initialCaseReader,
      EventListGroup eventListGroup) {
    AlertEventProcessor alertEventProcessor =
        new AlertEventProcessor(population, diseaseProperties, distributionSampler);
    VirusEventProcessor virusEventProcessor =
        new VirusEventProcessor(population, diseaseProperties, distributionSampler);
    InfectionEventProcessor infectionEventProcessor =
        new InfectionEventProcessor(population, diseaseProperties, distributionSampler);
    ContactEventProcessor contactEventProcessor =
        new ContactEventProcessor(
            population, diseaseProperties, distributionSampler, isolationPolicy);

    ProcessedEventsGrouper processedEventsGrouper = new ProcessedEventsGrouper();

    EventProcessorRunner<AlertEvent> alertEventRunner =
        new EventProcessorRunner<>(alertEventProcessor, processedEventsGrouper);
    EventProcessorRunner<ContactEvent> contactEventRunner =
        new EventProcessorRunner<>(contactEventProcessor, processedEventsGrouper);
    EventProcessorRunner<InfectionEvent> infectionEventRunner =
        new EventProcessorRunner<>(infectionEventProcessor, processedEventsGrouper);
    EventProcessorRunner<VirusEvent> virusEventRunner =
        new EventProcessorRunner<>(virusEventProcessor, processedEventsGrouper);

    InfectionCreator infectionCreator =
        new InfectionCreator(population, distributionSampler, initialCaseReader);
    return new EventRunnerImpl(
        alertEventRunner,
        contactEventRunner,
        infectionEventRunner,
        virusEventRunner,
        processedEventsGrouper,
        infectionCreator,
        eventListGroup);
  }

  @Bean
  public EventListWriter eventListWriter() {
    FormattedEventFactory formattedEventFactory = new FormattedEventFactory();
    return new EventListWriter(formattedEventFactory, completedEventList);
  }

  @Bean
  public LastContactTime lastContactTime(EventListGroup eventListGroup) {
    return new LastContactTime(eventListGroup);
  }
}
