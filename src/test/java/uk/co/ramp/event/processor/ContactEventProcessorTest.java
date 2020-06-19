package uk.co.ramp.event.processor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.ramp.*;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.Human;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.*;

@RunWith(SpringRunner.class)
@DirtiesContext
@Import({TestUtils.class, AppConfig.class, TestConfig.class})
public class ContactEventProcessorTest {
    @Rule
    public LogSpy logSpy = new LogSpy();

    @Autowired
    private ContactEventProcessor eventProcessor;

    @Before
    public void setUp() throws Exception {
        DiseaseProperties diseaseProperties = TestUtils.diseaseProperties();
        ReflectionTestUtils.setField(eventProcessor, "diseaseProperties", diseaseProperties);

        Human human = mock(Human.class);
        when(human.health()).thenReturn(-1d);
        Case aCase = new Case(human);

        Map<Integer, Case> population = new HashMap<>();
        population.put(0, aCase);
        ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));
    }

    @Test
    public void evaluateContact() {

        int infector = 0;
        int infectee = 1;
        int time = 0;

        Case mock0 = mock(Case.class);
        when(mock0.virusStatus()).thenReturn(SYMPTOMATIC);
        when(mock0.isInfectious()).thenReturn(true);
        when(mock0.id()).thenReturn(infector);

        Case mock1 = mock(Case.class);
        when(mock1.id()).thenReturn(infectee);
        when(mock1.virusStatus()).thenReturn(SUSCEPTIBLE);

        Map<Integer, Case> population = new HashMap<>();
        population.put(0, mock0);
        population.put(1, mock1);
        ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

        ContactEvent contactEvent = ImmutableContactEvent.builder().from(infector).to(infectee).time(time).weight(5000).label("").eventProcessor(eventProcessor).build();

        Optional<InfectionEvent> var = eventProcessor.evaluateContact(contactEvent, 0);

        Assert.assertTrue(var.isPresent());

        InfectionEvent infectionEvent = var.get();
        Assert.assertEquals(infectionEvent.time(), time + 1);
        Assert.assertEquals(infectionEvent.id(), infectee);
        Assert.assertEquals(infectionEvent.nextStatus(), EXPOSED);

    }

    @Test
    public void evaluateExposuresReturn() {

        int infector = 0;
        int infectee = 1;
        int time = 0;

        Case mock0 = mock(Case.class);
        when(mock0.virusStatus()).thenReturn(SYMPTOMATIC);
        when(mock0.isInfectious()).thenReturn(true);
        when(mock0.id()).thenReturn(infector);

        Case mock1 = mock(Case.class);
        when(mock1.id()).thenReturn(infectee);
        when(mock1.virusStatus()).thenReturn(SUSCEPTIBLE);

        Map<Integer, Case> population = new HashMap<>();
        population.put(0, mock0);
        population.put(1, mock1);
        ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

        // outer empty return
        ContactEvent contactEvent = ImmutableContactEvent.builder().from(infector).to(infectee).time(time).weight(5000).label("").eventProcessor(eventProcessor).build();

        Optional<InfectionEvent> var = eventProcessor.evaluateExposures(contactEvent, time);

        Assert.assertTrue(var.isPresent());

        InfectionEvent infectionEvent = var.get();
        Assert.assertEquals(infectionEvent.time(), time + 1);
        Assert.assertEquals(infectionEvent.id(), infectee);
        Assert.assertEquals(EXPOSED, infectionEvent.nextStatus());
        Assert.assertThat(logSpy.getOutput(), containsString("DANGER MIX"));

    }

    @Test
    public void evaluateExposuresEmpty() {

        int infector = 0;
        int infectee = 1;
        int time = 0;

        Case mock0 = mock(Case.class);
        when(mock0.virusStatus()).thenReturn(SUSCEPTIBLE);
        when(mock0.isInfectious()).thenReturn(false);
        when(mock0.id()).thenReturn(infector);

        Case mock1 = mock(Case.class);
        when(mock1.id()).thenReturn(infectee);
        when(mock1.virusStatus()).thenReturn(SUSCEPTIBLE);

        Map<Integer, Case> population = new HashMap<>();
        population.put(0, mock0);
        population.put(1, mock1);
        ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

        // outer empty return
        ContactEvent contactEvent = ImmutableContactEvent.builder().from(infector).to(infectee).time(time).weight(5000).label("").eventProcessor(eventProcessor).build();

        Optional<InfectionEvent> var = eventProcessor.evaluateExposures(contactEvent, time);

        System.out.println(logSpy.getOutput());
        Assert.assertFalse(var.isPresent());
        Assert.assertTrue(logSpy.getOutput().isEmpty());

    }

    @Test
    public void runContactEvents() {

        int infector = 0;
        int infectee = 1;

        Case mock0 = mock(Case.class);
        when(mock0.virusStatus()).thenReturn(SUSCEPTIBLE);
        when(mock0.isInfectious()).thenReturn(true);
        when(mock0.id()).thenReturn(infector);

        Case mock1 = mock(Case.class);
        when(mock1.id()).thenReturn(infectee);
        when(mock1.virusStatus()).thenReturn(SYMPTOMATIC);
        when(mock1.isInfectious()).thenReturn(true);

        Map<Integer, Case> population = new HashMap<>();
        population.put(0, mock0);
        population.put(1, mock1);

        ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

        ContactEvent event = ImmutableContactEvent.builder().time(0).to(0).from(1).weight(50000).label("").eventProcessor(eventProcessor).build();

        ProcessedEventResult processedEventResult = eventProcessor.processEvent(event);

        Assert.assertEquals(1, processedEventResult.newEvents().size());
        InfectionEvent evnt = (InfectionEvent) processedEventResult.newEvents().get(0);

        Assert.assertEquals(1, evnt.time());
        Assert.assertEquals(0, evnt.id());
        Assert.assertEquals(SUSCEPTIBLE, evnt.oldStatus());
        Assert.assertEquals(EXPOSED, evnt.nextStatus());

    }

}