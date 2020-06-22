package uk.co.ramp.event;

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
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.Human;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.AlertStatus.REQUESTED_TEST;
import static uk.co.ramp.people.VirusStatus.*;

@RunWith(SpringRunner.class)
@DirtiesContext
@Import({TestUtils.class, AppConfig.class, TestConfig.class})
public class VirusEventProcessorTest {
    private VirusEventProcessor eventProcessor;
    DiseaseProperties diseaseProperties;

    @Autowired
    DistributionSampler distributionSampler;

    @Before
    public void setUp() throws FileNotFoundException {
        diseaseProperties = TestUtils.diseaseProperties();

        Human human = mock(Human.class);
        when(human.health()).thenReturn(-1d);
        Case aCase = new Case(human);

        Map<Integer, Case> population = new HashMap<>();
        population.put(0, aCase);

        this.eventProcessor = new VirusEventProcessor(new Population(population), diseaseProperties, distributionSampler);
    }

    @Test
    public void checkForAlert() {

        VirusEvent virusEvent = ImmutableVirusEvent.builder().id(0).oldStatus(EXPOSED).nextStatus(PRESYMPTOMATIC).time(1).build();

        Assert.assertTrue(eventProcessor.checkForAlert(virusEvent).isEmpty());
        int time = 1;
        int id = 0;
        virusEvent = ImmutableVirusEvent.builder().id(id).oldStatus(PRESYMPTOMATIC).nextStatus(SYMPTOMATIC).time(time).build();

        Optional<AlertEvent> var = eventProcessor.checkForAlert(virusEvent);

        Assert.assertFalse(var.isEmpty());

        AlertEvent event = var.get();

        Assert.assertEquals(time + 1, event.time());
        Assert.assertEquals(NONE, event.oldStatus());
        Assert.assertEquals(REQUESTED_TEST, event.nextStatus());
        Assert.assertEquals(id, event.id());
    }

    @Test
    public void runVirusEvents() {

        int infector = 0;

        Case mock0 = mock(Case.class);
        when(mock0.virusStatus()).thenReturn(EXPOSED);
        when(mock0.isInfectious()).thenReturn(true);
        when(mock0.id()).thenReturn(infector);

        Map<Integer, Case> population = new HashMap<>();
        population.put(0, mock0);

        ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

        VirusEvent event = ImmutableVirusEvent.builder().time(0).id(0).oldStatus(EXPOSED).nextStatus(ASYMPTOMATIC).build();

        ProcessedEventResult processedEventResult = eventProcessor.processEvent(event);

        Assert.assertEquals(1, processedEventResult.newVirusEvents().size());
        Assert.assertEquals(0, processedEventResult.newAlertEvents().size());
        Assert.assertEquals(0, processedEventResult.newContactEvents().size());
        Assert.assertEquals(0, processedEventResult.newInfectionEvents().size());
        Assert.assertEquals(1, processedEventResult.completedEvents().size());

        VirusEvent evnt = processedEventResult.newVirusEvents().get(0);

        Assert.assertEquals(diseaseProperties.timeLatent().mean(), evnt.time());
        Assert.assertEquals(0, evnt.id());
        Assert.assertEquals(ASYMPTOMATIC, evnt.oldStatus());
        Assert.assertTrue(ASYMPTOMATIC.getValidTransitions().contains(evnt.nextStatus()));

    }

}