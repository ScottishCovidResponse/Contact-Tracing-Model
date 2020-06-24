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
import uk.co.ramp.event.types.Event;
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
  private EventListGroup eventListGroup;

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

  private AlertEvent mockAlertEvent3;
  private AlertEvent mockAlertEvent4;
  private ContactEvent mockContactEvent3;
  private ContactEvent mockContactEvent4;
  private InfectionEvent mockInfectionEvent3;
  private InfectionEvent mockInfectionEvent4;
  private VirusEvent mockVirusEvent3;
  private VirusEvent mockVirusEvent4;

  private Event completedEvent1;
  private Event completedEvent2;

  @Before
  public void setUp() {
    this.alertEventRunner = mock(MockAlertEventProcessorRunner.class);
    this.contactEventRunner = mock(MockContactEventProcessorRunner.class);
    this.infectionEventRunner = mock(MockInfectionEventProcessorRunner.class);
    this.virusEventRunner = mock(MockVirusEventProcessorRunner.class);
    this.processedEventsGrouper = mock(ProcessedEventsGrouper.class);
    this.infectionCreator = mock(InfectionCreator.class);
    this.eventListGroup = mock(EventListGroup.class);
    var mockAlertEvent1 = mock(AlertEvent.class);
    var mockAlertEvent2 = mock(AlertEvent.class);
    this.mockAlertEvent3 = mock(AlertEvent.class);
    this.mockAlertEvent4 = mock(AlertEvent.class);
    var mockContactEvent1 = mock(ContactEvent.class);
    var mockContactEvent2 = mock(ContactEvent.class);
    this.mockContactEvent3 = mock(ContactEvent.class);
    this.mockContactEvent4 = mock(ContactEvent.class);
    var mockInfectionEvent1 = mock(InfectionEvent.class);
    var mockInfectionEvent2 = mock(InfectionEvent.class);
    this.mockInfectionEvent3 = mock(InfectionEvent.class);
    this.mockInfectionEvent4 = mock(InfectionEvent.class);
    var mockInfectionEvent5 = mock(InfectionEvent.class);
    var mockInfectionEvent6 = mock(InfectionEvent.class);
    var mockVirusEvent1 = mock(VirusEvent.class);
    var mockVirusEvent2 = mock(VirusEvent.class);
    this.mockVirusEvent3 = mock(VirusEvent.class);
    this.mockVirusEvent4 = mock(VirusEvent.class);
    var mockProcessedAlertEventValue1 = mock(ProcessedEventResult.class);
    var mockProcessedAlertEventValue2 = mock(ProcessedEventResult.class);
    var mockProcessedAlertEventValue3 = mock(ProcessedEventResult.class);
    var mockProcessedAlertEventValue4 = mock(ProcessedEventResult.class);
    var mockProcessedAlertEventValue5 = mock(ProcessedEventResult.class);
    var mockProcessedAlertEventValue6 = mock(ProcessedEventResult.class);
    var mockAggregatedProcessedEvent = mock(ProcessedEventResult.class);
    this.completedEvent1 = mock(Event.class);
    this.completedEvent2 = mock(Event.class);

    var alertEvents = List.of(mockAlertEvent1, mockAlertEvent2);
    var contactEvents = List.of(mockContactEvent1, mockContactEvent2);
    var infectionEvents = List.of(mockInfectionEvent1, mockInfectionEvent2);
    var initialInfectionEvents = List.of(mockInfectionEvent5);
    var virusEvents = List.of(mockVirusEvent1, mockVirusEvent2);
    var randomInfectionEvents = List.of(mockInfectionEvent6);

    when(eventListGroup.getAlertEvents(eq(0))).thenReturn(alertEvents);
    when(eventListGroup.getContactEvents(eq(0))).thenReturn(contactEvents);
    when(eventListGroup.getInfectionEvents(eq(0))).thenReturn(infectionEvents);
    when(eventListGroup.getVirusEvents(eq(0))).thenReturn(virusEvents);

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
    when(mockAggregatedProcessedEvent.completedEvents())
        .thenReturn(List.of(completedEvent1, completedEvent2));

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
            eventListGroup);

    eventRunner.run(0, 0, 0);

    verify(eventListGroup).addContactEvents(List.of(mockContactEvent3, mockContactEvent4));
    verify(eventListGroup).addInfectionEvents(List.of(mockInfectionEvent3, mockInfectionEvent4));
    verify(eventListGroup).addAlertEvents(List.of(mockAlertEvent3, mockAlertEvent4));
    verify(eventListGroup).addVirusEvents(List.of(mockVirusEvent3, mockVirusEvent4));
    verify(eventListGroup).completed(List.of(completedEvent1, completedEvent2));
  }
}
