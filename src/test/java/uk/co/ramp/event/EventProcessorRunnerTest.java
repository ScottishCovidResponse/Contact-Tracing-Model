package uk.co.ramp.event;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static uk.co.ramp.people.VirusStatus.*;

import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ramp.*;
import uk.co.ramp.event.types.*;

@RunWith(SpringRunner.class)
@DirtiesContext
@Import({TestUtils.class, AppConfig.class, TestConfig.class})
public class EventProcessorRunnerTest {
  private interface MockEventProcessor extends EventProcessor<Event> {}

  @Rule public LogSpy logSpy = new LogSpy();

  private EventProcessorRunner<Event> eventProcessorRunner;
  private EventProcessor<Event> eventProcessor;
  private ProcessedEventsGrouper processedEventsGrouper;

  private Event mockEvent1;
  private Event mockEvent2;
  private ProcessedEventResult mockProcessedEventResult3;

  @Before
  public void setUp() throws Exception {
    this.eventProcessor = mock(MockEventProcessor.class);
    this.processedEventsGrouper = mock(ProcessedEventsGrouper.class);
    this.mockEvent1 = mock(Event.class);
    this.mockEvent2 = mock(Event.class);
    var mockProcessedEventResult1 = mock(ProcessedEventResult.class);
    var mockProcessedEventResult2 = mock(ProcessedEventResult.class);
    this.mockProcessedEventResult3 = mock(ProcessedEventResult.class);

    when(processedEventsGrouper.groupProcessedEventResults(
            eq(List.of(mockProcessedEventResult1, mockProcessedEventResult2))))
        .thenReturn(mockProcessedEventResult3);
    when(eventProcessor.processEvent(eq(mockEvent1))).thenReturn(mockProcessedEventResult1);
    when(eventProcessor.processEvent(eq(mockEvent2))).thenReturn(mockProcessedEventResult2);
  }

  @Test
  public void run() {
    eventProcessorRunner = new EventProcessorRunner<>(eventProcessor, processedEventsGrouper);
    assertThat(eventProcessorRunner.run(List.of(mockEvent1, mockEvent2)))
        .isEqualTo(mockProcessedEventResult3);
  }
}
