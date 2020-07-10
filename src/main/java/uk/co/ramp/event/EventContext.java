package uk.co.ramp.event;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.EventRunner;
import uk.co.ramp.event.types.InfectionEvent;
import uk.co.ramp.event.types.VirusEvent;
import uk.co.ramp.io.InitialCaseReader;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.OutputFolder;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.policy.alert.AlertChecker;
import uk.co.ramp.policy.isolation.IsolationPolicy;

@SpringBootConfiguration
public class EventContext {

  @Bean
  public CompletionEventListGroup eventList() {
    EventList<AlertEvent> alertEventList = new EventList<>();
    EventList<ContactEvent> contactEventList = new EventList<>();
    EventList<InfectionEvent> infectionEventList = new EventList<>();
    EventList<VirusEvent> virusEventList = new EventList<>();
    EventList<AlertEvent> completedAlertEventList = new EventList<>();
    EventList<ContactEvent> completedContactEventList = new EventList<>();
    EventList<InfectionEvent> completedInfectionEventList = new EventList<>();
    EventList<VirusEvent> completedVirusEventList = new EventList<>();
    EventListGroup newEventListGroup =
        new EventListGroup(alertEventList, contactEventList, infectionEventList, virusEventList);
    EventListGroup completedEventListGroup =
        new EventListGroup(
            completedAlertEventList,
            completedContactEventList,
            completedInfectionEventList,
            completedVirusEventList);
    return new CompletionEventListGroup(newEventListGroup, completedEventListGroup);
  }

  @Bean
  public EventRunner eventRunner(
      Population population,
      StandardProperties properties,
      DiseaseProperties diseaseProperties,
      DistributionSampler distributionSampler,
      IsolationPolicy isolationPolicy,
      InitialCaseReader initialCaseReader,
      AlertChecker alertChecker,
      CompletionEventListGroup eventList) {
    AlertEventProcessor alertEventProcessor =
        new AlertEventProcessor(population, properties, diseaseProperties, distributionSampler);
    VirusEventProcessor virusEventProcessor =
        new VirusEventProcessor(population, properties, diseaseProperties, distributionSampler, alertChecker);
    InfectionEventProcessor infectionEventProcessor =
        new InfectionEventProcessor(population, properties, diseaseProperties, distributionSampler);
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
        eventList);
  }

  @Bean
  public EventListWriter eventListWriter(
      CompletionEventListGroup eventList, OutputFolder outputFolder, StandardProperties properties) {
    FormattedEventFactory formattedEventFactory = new FormattedEventFactory();
    return new EventListWriter(formattedEventFactory, eventList, properties, outputFolder.outputFolder());
  }

  @Bean
  public LastContactTime lastContactTime(CompletionEventListGroup eventList) {
    return new LastContactTime(eventList);
  }
}
