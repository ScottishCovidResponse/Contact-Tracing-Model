package uk.co.ramp.event;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.co.ramp.event.types.*;

public class EventRunnerImpl implements EventRunner {
  private final EventProcessorRunner<AlertEvent> alertEventRunner;
  private final EventProcessorRunner<ContactEvent> contactEventRunner;
  private final EventProcessorRunner<InfectionEvent> infectionEventRunner;
  private final EventProcessorRunner<VirusEvent> virusEventRunner;
  private final ProcessedEventsGrouper processedEventsGrouper;
  private final InfectionCreator infectionCreator;
  private final CompletionEventListGroup eventList;

  public EventRunnerImpl(
      EventProcessorRunner<AlertEvent> alertEventRunner,
      EventProcessorRunner<ContactEvent> contactEventRunner,
      EventProcessorRunner<InfectionEvent> infectionEventRunner,
      EventProcessorRunner<VirusEvent> virusEventRunner,
      ProcessedEventsGrouper processedEventsGrouper,
      InfectionCreator infectionCreator,
      CompletionEventListGroup eventList) {
    this.alertEventRunner = alertEventRunner;
    this.contactEventRunner = contactEventRunner;
    this.infectionEventRunner = infectionEventRunner;
    this.virusEventRunner = virusEventRunner;
    this.processedEventsGrouper = processedEventsGrouper;
    this.infectionCreator = infectionCreator;
    this.eventList = eventList;
  }

  @Override
  public void run(int time, double randomInfectionRate, double randomCutOff) {
    List<ProcessedEventResult> processedResults =
        Stream.of(
                infectionEventRunner.run(generateInitialInfection(time)),
                alertEventRunner.run(eventListGroup.getAlertEvents(time)),
                contactEventRunner.run(eventListGroup.getContactEvents(time)),
                infectionEventRunner.run(eventListGroup.getInfectionEvents(time)),
                virusEventRunner.run(eventListGroup.getVirusEvents(time)),
                infectionEventRunner.run(
                    createRandomInfections(time, randomInfectionRate, randomCutOff)))
            .collect(Collectors.toList());

    ProcessedEventResult eventResults =
        processedEventsGrouper.groupProcessedEventResults(processedResults);

    eventList.addNewContactEvents(eventResults.newContactEvents());
    eventList.addNewAlertEvents(eventResults.newAlertEvents());
    eventList.addNewInfectionEvents(eventResults.newInfectionEvents());
    eventList.addNewVirusEvents(eventResults.newVirusEvents());
    eventList.addCompletedContactEvents(eventResults.newCompletedContactEvents());
    eventList.addCompletedAlertEvents(eventResults.newCompletedAlertEvents());
    eventList.addCompletedInfectionEvents(eventResults.newCompletedInfectionEvents());
    eventList.addCompletedVirusEvents(eventResults.newCompletedVirusEvents());
  }

  List<InfectionEvent> createRandomInfections(
      int time, double randomInfectionRate, double randomCutOff) {
    return infectionCreator.createRandomInfections(time, randomInfectionRate, randomCutOff);
  }

  List<InfectionEvent> generateInitialInfection(int time) {
    return infectionCreator.generateInitialInfections(time);
  }
}
