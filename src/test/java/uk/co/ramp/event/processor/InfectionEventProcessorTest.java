package uk.co.ramp.event.processor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.ramp.AppConfig;
import uk.co.ramp.Population;
import uk.co.ramp.TestConfig;
import uk.co.ramp.TestUtils;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.ImmutableInfectionEvent;
import uk.co.ramp.event.types.InfectionEvent;
import uk.co.ramp.event.types.ProcessedEventResult;
import uk.co.ramp.event.types.VirusEvent;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.Human;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.EXPOSED;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;
import static uk.co.ramp.people.VirusStatus.SYMPTOMATIC;

@RunWith(SpringRunner.class)
@DirtiesContext
@Import({TestUtils.class, AppConfig.class, TestConfig.class})
public class InfectionEventProcessorTest {
    private InfectionEventProcessor eventProcessor;
    private DiseaseProperties diseaseProperties;

    @Autowired
    private DistributionSampler distributionSampler;

    private Case thisCase;

    private InfectionEvent event;

    @Before
    public void setUp() throws FileNotFoundException {
        diseaseProperties = TestUtils.diseaseProperties();

        thisCase = mock(Case.class);
        when(thisCase.virusStatus()).thenReturn(SUSCEPTIBLE);
        when(thisCase.health()).thenReturn(-1d);

        Map<Integer, Case> population = new HashMap<>();
        population.put(0, thisCase);

        this.eventProcessor = new InfectionEventProcessor(new Population(population), diseaseProperties, distributionSampler, mock(VirusEventProcessor.class));

        event = ImmutableInfectionEvent.builder()
                .exposedBy(10)
                .exposedTime(5)
                .id(0)
                .nextStatus(EXPOSED)
                .oldStatus(SUSCEPTIBLE)
                .eventProcessor(eventProcessor)
                .time(3)
                .build();
    }

    @Test
    public void runInfectionEvents() {

        int infector = 0;
        int infectee = 1;

        Case mock0 = mock(Case.class);
        when(mock0.virusStatus()).thenReturn(SUSCEPTIBLE);
        when(mock0.isInfectious()).thenReturn(true);
        when(mock0.id()).thenReturn(infector);

        Case mock1 = mock(Case.class);
        when(mock1.id()).thenReturn(infectee);
        when(mock1.virusStatus()).thenReturn(SYMPTOMATIC);

        Map<Integer, Case> population = new HashMap<>();
        population.put(0, mock0);
        population.put(1, mock1);
        ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

        InfectionEvent event = ImmutableInfectionEvent.builder().time(0).exposedBy(1).id(0).exposedTime(0).oldStatus(SUSCEPTIBLE).nextStatus(EXPOSED).eventProcessor(eventProcessor).build();

        ProcessedEventResult processedEventResult = eventProcessor.processEvent(event);

        Assert.assertEquals(1, processedEventResult.newEvents().size());
        VirusEvent evnt = (VirusEvent) processedEventResult.newEvents().get(0);

        Assert.assertEquals(diseaseProperties.timeLatent().mean(), evnt.time());
        Assert.assertEquals(0, evnt.id());
        Assert.assertEquals(EXPOSED, evnt.oldStatus());
        Assert.assertTrue(EXPOSED.getValidTransitions().contains(evnt.nextStatus()));

    }

    @Test
    public void testInfectionEventExposedBySetInCase() {
        event.processEvent();
        verify(thisCase).setExposedBy(10);
    }

    @Test
    public void testInfectionEventExposedTimeSetInCase() {
        event.processEvent();
        verify(thisCase).setExposedTime(5);
    }

    @Test
    public void testInfectionEventVirusStatusSetInCase() {
        event.processEvent();
        verify(thisCase).setVirusStatus(EXPOSED);
    }

}