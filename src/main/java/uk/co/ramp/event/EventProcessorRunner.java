package uk.co.ramp.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.processor.InfectionEventProcessor;
import uk.co.ramp.event.types.*;
import uk.co.ramp.people.Case;

import java.util.*;
import java.util.stream.Collectors;

import static uk.co.ramp.people.VirusStatus.EXPOSED;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

@Service
public class EventProcessorRunner {
    private final Population population;
    private final EventList eventList;
    private final DistributionSampler distributionSampler;
    private final InfectionEventProcessor infectionEventProcessor;

    @Autowired
    public EventProcessorRunner(Population population, DistributionSampler distributionSampler, EventList eventList, InfectionEventProcessor infectionEventProcessor) {
        this.population = population;
        this.distributionSampler = distributionSampler;
        this.eventList = eventList;
        this.infectionEventProcessor = infectionEventProcessor;
    }

    public void process(int time, double randomInfectionRate, int randomCutoff) {

        List<Event> newEvents = new ArrayList<>(runAllEvents(time));
        newEvents.addAll(runPolicyEvents(time));

        if (randomInfectionRate > 0d && time < randomCutoff) {
            newEvents.addAll(createRandomInfections(time, randomInfectionRate));
        }

        eventList.addEvents(newEvents);

    }

    List<Event> runAllEvents(int time) {
        List<ProcessedEventResult> processedEventResults = eventList.getForTime(time).stream()
                .map(Event::processEvent)
                .collect(Collectors.toList());

        processedEventResults.stream()
                .map(ProcessedEventResult::completedEvent)
                .flatMap(Optional::stream)
                .forEach(eventList::completed);

        return processedEventResults.stream()
                .map(ProcessedEventResult::newEvents)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    List<InfectionEvent> createRandomInfections(int time, double randomRate) {

        List<Case> sus = population.view().values().stream().filter(aCase -> aCase.virusStatus() == SUSCEPTIBLE).collect(Collectors.toList());
        List<InfectionEvent> randomInfections = new ArrayList<>();
        for (Case aCase : sus) {
            if (distributionSampler.uniformBetweenZeroAndOne() < randomRate) {
                randomInfections.add(
                        ImmutableInfectionEvent.builder().
                                time(time + 1).id(aCase.id()).
                                nextStatus(EXPOSED).
                                oldStatus(SUSCEPTIBLE).
                                exposedTime(time).
                                exposedBy(Case.getRandomInfection()).
                                eventProcessor(infectionEventProcessor).
                                build());
            }
        }

        return randomInfections;
    }

    List<Event> runPolicyEvents(int time) {
        //TODO
        return new ArrayList<>();
    }
}
