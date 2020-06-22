package uk.co.ramp.event;

import uk.co.ramp.event.types.Event;
import uk.co.ramp.event.types.EventProcessor;
import uk.co.ramp.event.types.ProcessedEventResult;

import java.util.List;
import java.util.stream.Collectors;

public class EventProcessorRunner<T extends Event> {
    private final EventProcessor<T> eventProcessor;
    private final ProcessedEventsGrouper processedEventsGrouper;

    public EventProcessorRunner(EventProcessor<T> eventProcessor, ProcessedEventsGrouper processedEventsGrouper) {
        this.eventProcessor = eventProcessor;
        this.processedEventsGrouper = processedEventsGrouper;
    }

    public ProcessedEventResult run(List<T> eventsToProcess) {
        List<ProcessedEventResult> processedEventResults = eventsToProcess.stream()
                .map(eventProcessor::processEvent)
                .collect(Collectors.toList());

        return processedEventsGrouper.groupProcessedEventResults(processedEventResults);
    }
}
