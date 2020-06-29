package uk.co.ramp.event;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.EventProcessor;
import uk.co.ramp.event.types.InfectionEvent;
import uk.co.ramp.event.types.ProcessedEventResult;
import uk.co.ramp.event.types.VirusEvent;

public class EventRunnerImplTest {
  private EventProcessorRunner<AlertEvent> alertEventRunner;
  private EventProcessorRunner<ContactEvent> contactEventRunner;
  private EventProcessorRunner<InfectionEvent> infectionEventRunner;
  private EventProcessorRunner<VirusEvent> virusEventRunner;
  private ProcessedEventsGrouper processedEventsGrouper;
  private InfectionCreator infectionCreator;
  private CompletionEventListGroup eventList;
  private AlertEvent mockAlertEvent1;
  private AlertEvent mockAlertEvent2;
  private ContactEvent mockContactEvent1;
  private ContactEvent mockContactEvent2;
  private InfectionEvent mockInfectionEvent1;
  private InfectionEvent mockInfectionEvent2;
  private VirusEvent mockVirusEvent1;
  private VirusEvent mockVirusEvent2;
  private AlertEvent mockAlertEvent3;
  private AlertEvent mockAlertEvent4;
  private ContactEvent mockContactEvent3;
  private ContactEvent mockContactEvent4;
  private InfectionEvent mockInfectionEvent3;
  private InfectionEvent mockInfectionEvent4;
  private VirusEvent mockVirusEvent3;
  private VirusEvent mockVirusEvent4;

  private static class MockAlertEventProcessorRunner extends EventProcessorRunner<AlertEvent> {
    public MockAlertEventProcessorRunner(
        EventProcessor<AlertEvent> eventProcessor, ProcessedEventsGrouper processedEventsGrouper) {
      super(eventProcessor, processedEventsGrouper);
    }
  }

  private static class MockContactEventProcessorRunner extends EventProcessorRunner<ContactEvent> {
    public MockContactEventProcessorRunner(
        EventProcessor<ContactEvent> eventProcessor,
        ProcessedEventsGrouper processedEventsGrouper) {
      super(eventProcessor, processedEventsGrouper);
    }
  }

  private static class MockInfectionEventProcessorRunner
      extends EventProcessorRunner<InfectionEvent> {
    public MockInfectionEventProcessorRunner(
        EventProcessor<InfectionEvent> eventProcessor,
        ProcessedEventsGrouper processedEventsGrouper) {
      super(eventProcessor, processedEventsGrouper);
    }
  }

  private static class MockVirusEventProcessorRunner extends EventProcessorRunner<VirusEvent> {
    public MockVirusEventProcessorRunner(
        EventProcessor<VirusEvent> eventProcessor, ProcessedEventsGrouper processedEventsGrouper) {
      super(eventProcessor, processedEventsGrouper);
    }
  }

  @Before
  public void setUp() {
    this.alertEventRunner = mock(MockAlertEventProcessorRunner.class);
    this.contactEventRunner = mock(MockContactEventProcessorRunner.class);
    this.infectionEventRunner = mock(MockInfectionEventProcessorRunner.class);
    this.virusEventRunner = mock(MockVirusEventProcessorRunner.class);
    this.processedEventsGrouper = mock(ProcessedEventsGrouper.class);
    this.infectionCreator = mock(InfectionCreator.class);
    this.eventList = mock(CompletionEventListGroup.class);
    this.mockAlertEvent1 = mock(AlertEvent.class);
    this.mockAlertEvent2 = mock(AlertEvent.class);
    this.mockAlertEvent3 = mock(AlertEvent.class);
    this.mockAlertEvent4 = mock(AlertEvent.class);
    this.mockContactEvent1 = mock(ContactEvent.class);
    this.mockContactEvent2 = mock(ContactEvent.class);
    this.mockContactEvent3 = mock(ContactEvent.class);
    this.mockContactEvent4 = mock(ContactEvent.class);
    this.mockInfectionEvent1 = mock(InfectionEvent.class);
    this.mockInfectionEvent2 = mock(InfectionEvent.class);
    this.mockInfectionEvent3 = mock(InfectionEvent.class);
    this.mockInfectionEvent4 = mock(InfectionEvent.class);
    var mockInfectionEvent5 = mock(InfectionEvent.class);
    var mockInfectionEvent6 = mock(InfectionEvent.class);
    this.mockVirusEvent1 = mock(VirusEvent.class);
    this.mockVirusEvent2 = mock(VirusEvent.class);
    this.mockVirusEvent3 = mock(VirusEvent.class);
    this.mockVirusEvent4 = mock(VirusEvent.class);
    var mockProcessedAlertEventValue1 = mock(ProcessedEventResult.class);
    var mockProcessedAlertEventValue2 = mock(ProcessedEventResult.class);
    var mockProcessedAlertEventValue3 = mock(ProcessedEventResult.class);
    var mockProcessedAlertEventValue4 = mock(ProcessedEventResult.class);
    var mockProcessedAlertEventValue5 = mock(ProcessedEventResult.class);
    var mockProcessedAlertEventValue6 = mock(ProcessedEventResult.class);
    var mockAggregatedProcessedEvent = mock(ProcessedEventResult.class);

    var alertEvents = List.of(mockAlertEvent1, mockAlertEvent2);
    var contactEvents = List.of(mockContactEvent1, mockContactEvent2);
    var infectionEvents = List.of(mockInfectionEvent1, mockInfectionEvent2);
    var initialInfectionEvents = List.of(mockInfectionEvent5);
    var virusEvents = List.of(mockVirusEvent1, mockVirusEvent2);
    var randomInfectionEvents = List.of(mockInfectionEvent6);

    when(eventList.getNewAlertEvents(eq(0))).thenReturn(alertEvents);
    when(eventList.getNewContactEvents(eq(0))).thenReturn(contactEvents);
    when(eventList.getNewInfectionEvents(eq(0))).thenReturn(infectionEvents);
    when(eventList.getNewVirusEvents(eq(0))).thenReturn(virusEvents);

    when(infectionCreator.generateInitialInfections(eq(0))).thenReturn(initialInfectionEvents);
    when(infectionCreator.createRandomInfections(eq(0), eq(0D), eq(0D)))
        .thenReturn(randomInfectionEvents);

    when(alertEventRunner.run(eq(alertEvents))).thenReturn(mockProcessedAlertEventValue1);
    when(contactEventRunner.run(eq(contactEvents))).thenReturn(mockProcessedAlertEventValue2);
    when(infectionEventRunner.run(eq(infectionEvents))).thenReturn(mockProcessedAlertEventValue3);
    when(infectionEventRunner.run(eq(initialInfectionEvents)))
        .thenReturn(mockProcessedAlertEventValue5);
    when(infectionEventRunner.run(eq(randomInfectionEvents)))
        .thenReturn(mockProcessedAlertEventValue6);
    when(virusEventRunner.run(eq(virusEvents))).thenReturn(mockProcessedAlertEventValue4);

    when(mockAggregatedProcessedEvent.newAlertEvents())
        .thenReturn(List.of(mockAlertEvent3, mockAlertEvent4));
    when(mockAggregatedProcessedEvent.newContactEvents())
        .thenReturn(List.of(mockContactEvent3, mockContactEvent4));
    when(mockAggregatedProcessedEvent.newInfectionEvents())
        .thenReturn(List.of(mockInfectionEvent3, mockInfectionEvent4));
    when(mockAggregatedProcessedEvent.newVirusEvents())
        .thenReturn(List.of(mockVirusEvent3, mockVirusEvent4));
    when(mockAggregatedProcessedEvent.newCompletedAlertEvents()).thenReturn(alertEvents);
    when(mockAggregatedProcessedEvent.newCompletedContactEvents()).thenReturn(contactEvents);
    when(mockAggregatedProcessedEvent.newCompletedInfectionEvents()).thenReturn(infectionEvents);
    when(mockAggregatedProcessedEvent.newCompletedVirusEvents()).thenReturn(virusEvents);

    var processedEvents =
        List.of(
            mockProcessedAlertEventValue5,
            mockProcessedAlertEventValue1,
            mockProcessedAlertEventValue2,
            mockProcessedAlertEventValue3,
            mockProcessedAlertEventValue4,
            mockProcessedAlertEventValue6);
    when(processedEventsGrouper.groupProcessedEventResults(eq(processedEvents)))
        .thenReturn(mockAggregatedProcessedEvent);
  }

  @Test
  public void testRun() {
    EventRunnerImpl eventRunner =
        new EventRunnerImpl(
            alertEventRunner,
            contactEventRunner,
            infectionEventRunner,
            virusEventRunner,
            processedEventsGrouper,
            infectionCreator,
            eventList);

    eventRunner.run(0, 0, 0);

    verify(eventList).addNewContactEvents(List.of(mockContactEvent3, mockContactEvent4));
    verify(eventList).addNewInfectionEvents(List.of(mockInfectionEvent3, mockInfectionEvent4));
    verify(eventList).addNewAlertEvents(List.of(mockAlertEvent3, mockAlertEvent4));
    verify(eventList).addNewVirusEvents(List.of(mockVirusEvent3, mockVirusEvent4));
    verify(eventList).addCompletedAlertEvents(List.of(mockAlertEvent1, mockAlertEvent2));
    verify(eventList).addCompletedContactEvents(List.of(mockContactEvent1, mockContactEvent2));
    verify(eventList)
        .addCompletedInfectionEvents(List.of(mockInfectionEvent1, mockInfectionEvent2));
    verify(eventList).addCompletedVirusEvents(List.of(mockVirusEvent1, mockVirusEvent2));
  }
}
