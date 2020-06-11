package uk.co.ramp.event;

import org.assertj.core.error.ShouldHaveSizeBetween;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.ramp.AppConfig;
import uk.co.ramp.LogSpy;
import uk.co.ramp.TestConfig;
import uk.co.ramp.TestUtils;
import uk.co.ramp.distribution.ProgressionDistribution;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.Human;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.utilities.ImmutableMeanMax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static uk.co.ramp.people.AlertStatus.*;
import static uk.co.ramp.people.VirusStatus.*;

@RunWith(SpringRunner.class)
@DirtiesContext
@Import({TestUtils.class, AppConfig.class, TestConfig.class})
public class EventProcessorTest {


    @Rule
    public LogSpy logSpy = new LogSpy();


    DiseaseProperties diseaseProperties;

    @Autowired
    private EventProcessor eventProcessor;
    @Autowired
    private EventList eventList;


    @Before
    public void setUp() throws Exception {
        diseaseProperties = TestUtils.diseaseProperties();
        ReflectionTestUtils.setField(eventProcessor, "diseaseProperties", diseaseProperties);
    }


    @Test
    public void process() {

        eventProcessor = mock(EventProcessor.class);
        doCallRealMethod().when(eventProcessor).process(anyInt(), anyDouble(), anyInt());
        ReflectionTestUtils.setField(eventProcessor, "eventList", eventList);

        eventProcessor.process(0, 0.1, 1);

        verify(eventProcessor, times(1)).runInfectionEvents(anyInt());
        verify(eventProcessor, times(1)).runVirusEvents(anyInt());
        verify(eventProcessor, times(1)).runContactEvents(anyInt());
        verify(eventProcessor, times(1)).runAlertEvents(anyInt());
        verify(eventProcessor, times(1)).runPolicyEvents(anyInt());
        verify(eventProcessor, times(1)).createRandomInfections(anyInt(), anyDouble());

    }


    @Test
    public void createRandomInfections() {

        Map<Integer, Case> population = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            Case mock0 = mock(Case.class);
            when(mock0.virusStatus()).thenReturn(SUSCEPTIBLE);
            when(mock0.id()).thenReturn(i);
            population.put(i, mock0);
        }

        eventProcessor.setPopulation(population);

        List<InfectionEvent> list = eventProcessor.createRandomInfections(0, 0.1);

        ShouldHaveSizeBetween.shouldHaveSizeBetween(list, list.size(), 95, 105);

        assertThat(list.size()).isBetween(95, 105);


    }

    @Test
    public void runAlertEvents() {
        int infector = 0;


        Case mock0 = mock(Case.class);
        when(mock0.virusStatus()).thenReturn(SUSCEPTIBLE);
        when(mock0.isInfectious()).thenReturn(true);
        when(mock0.id()).thenReturn(infector);

        Map<Integer, Case> population = new HashMap<>();
        population.put(0, mock0);

        eventProcessor.setPopulation(population);

        AlertEvent event = ImmutableAlertEvent.builder().time(0).id(0).oldStatus(NONE).nextStatus(ALERTED).build();
        eventList.addEvent(event);

        ReflectionTestUtils.setField(eventProcessor, "eventList", eventList);

        List<AlertEvent> list = eventProcessor.runAlertEvents(0);

        System.out.println(list);

        Assert.assertEquals(1, list.size());
        AlertEvent evnt = list.get(0);

        Assert.assertEquals(1, evnt.time());
        Assert.assertEquals(0, evnt.id());
        Assert.assertEquals(ALERTED, evnt.oldStatus());
        Assert.assertEquals(REQUESTED_TEST, evnt.nextStatus());


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

        eventProcessor.setPopulation(population);

        ContactEvent event = ImmutableContactEvent.builder().time(0).to(0).from(1).weight(50000).label("").build();
        eventList.addEvent(event);

        ReflectionTestUtils.setField(eventProcessor, "eventList", eventList);

        List<InfectionEvent> list = eventProcessor.runContactEvents(0);

        Assert.assertEquals(1, list.size());
        InfectionEvent evnt = list.get(0);

        Assert.assertEquals(1, evnt.time());
        Assert.assertEquals(0, evnt.id());
        Assert.assertEquals(SUSCEPTIBLE, evnt.oldStatus());
        Assert.assertEquals(EXPOSED, evnt.nextStatus());

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

        eventProcessor.setPopulation(population);

        VirusEvent event = ImmutableVirusEvent.builder().time(0).id(0).oldStatus(EXPOSED).nextStatus(ASYMPTOMATIC).build();
        eventList.addEvent(event);

        ReflectionTestUtils.setField(eventProcessor, "eventList", eventList);

        List<Event> list = eventProcessor.runVirusEvents(0);

        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.get(0) instanceof VirusEvent);
        VirusEvent evnt = (VirusEvent) list.get(0);

        Assert.assertEquals(diseaseProperties.timeLatent().mean(), evnt.time());
        Assert.assertEquals(0, evnt.id());
        Assert.assertEquals(ASYMPTOMATIC, evnt.oldStatus());
        Assert.assertTrue(ASYMPTOMATIC.getValidTransitions().contains(evnt.nextStatus()));

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
        eventProcessor.setPopulation(population);

        InfectionEvent event = ImmutableInfectionEvent.builder().time(0).exposedBy(1).id(0).exposedTime(0).oldStatus(SUSCEPTIBLE).nextStatus(EXPOSED).build();
        eventList.addEvent(event);

        ReflectionTestUtils.setField(eventProcessor, "eventList", eventList);

        List<VirusEvent> list = eventProcessor.runInfectionEvents(0);

        Assert.assertEquals(1, list.size());
        VirusEvent evnt = list.get(0);

        Assert.assertEquals(diseaseProperties.timeLatent().mean(), evnt.time());
        Assert.assertEquals(0, evnt.id());
        Assert.assertEquals(EXPOSED, evnt.oldStatus());
        Assert.assertTrue(EXPOSED.getValidTransitions().contains(evnt.nextStatus()));

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
        eventProcessor.setPopulation(population);

        ContactEvent contactEvent = ImmutableContactEvent.builder().from(infector).to(infectee).time(time).weight(5000).label("").build();

        Optional<InfectionEvent> var = eventProcessor.evaluateContact(time, contactEvent, 0);

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
        eventProcessor.setPopulation(population);

        // outer empty return
        ContactEvent contactEvent = ImmutableContactEvent.builder().from(infector).to(infectee).time(time).weight(5000).label("").build();

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
        eventProcessor.setPopulation(population);

        // outer empty return
        ContactEvent contactEvent = ImmutableContactEvent.builder().from(infector).to(infectee).time(time).weight(5000).label("").build();

        Optional<InfectionEvent> var = eventProcessor.evaluateExposures(contactEvent, time);

        System.out.println(logSpy.getOutput());
        Assert.assertFalse(var.isPresent());
        Assert.assertTrue(logSpy.getOutput().isEmpty());

    }

    @Test
    public void checkForAlert() {

        VirusEvent virusEvent = ImmutableVirusEvent.builder().id(0).oldStatus(EXPOSED).nextStatus(PRESYMPTOMATIC).time(1).build();

        Assert.assertTrue(eventProcessor.checkForAlert(virusEvent).isEmpty());
        int time = 1;
        int id = 0;
        virusEvent = ImmutableVirusEvent.builder().id(id).oldStatus(PRESYMPTOMATIC).nextStatus(SYMPTOMATIC).time(time).build();

        Optional<Event> var = eventProcessor.checkForAlert(virusEvent);

        Assert.assertFalse(var.isEmpty());

        AlertEvent event = (AlertEvent) var.get();

        Assert.assertEquals(time + 1, event.time());
        Assert.assertEquals(NONE, event.oldStatus());
        Assert.assertEquals(REQUESTED_TEST, event.nextStatus());
        Assert.assertEquals(id, event.id());
    }

    @Test
    public void determineNextStatus() {
        Case mock = mock(Case.class);
        Map<Integer, Case> population = new HashMap<>();
        population.put(0, mock);
        eventProcessor.setPopulation(population);
        CommonVirusEvent commonVirusEvent = ImmutableVirusEvent.builder().id(0).oldStatus(EXPOSED).nextStatus(EXPOSED).time(1).build();
        VirusStatus var = eventProcessor.determineNextStatus(commonVirusEvent);

        Assert.assertTrue(EXPOSED.getValidTransitions().contains(var));

        commonVirusEvent = ImmutableVirusEvent.builder().id(0).oldStatus(EXPOSED).nextStatus(SYMPTOMATIC).time(1).build();
        var = eventProcessor.determineNextStatus(commonVirusEvent);

        Assert.assertTrue(SYMPTOMATIC.getValidTransitions().contains(var));

        commonVirusEvent = ImmutableVirusEvent.builder().id(0).oldStatus(EXPOSED).nextStatus(SEVERELY_SYMPTOMATIC).time(1).build();
        var = eventProcessor.determineNextStatus(commonVirusEvent);

        Assert.assertTrue(SEVERELY_SYMPTOMATIC.getValidTransitions().contains(var));

    }

    @Test
    public void timeInCompartment() {
        diseaseProperties = Mockito.mock(DiseaseProperties.class);

        int i = 0;

        when(diseaseProperties.progressionDistribution()).thenReturn(ProgressionDistribution.FLAT);
        when(diseaseProperties.timeLatent()).thenReturn(ImmutableMeanMax.builder().mean(++i).max(++i).build());
        when(diseaseProperties.timeRecoveryAsymp()).thenReturn(ImmutableMeanMax.builder().mean(++i).max(++i).build());
        when(diseaseProperties.timeRecoverySymp()).thenReturn(ImmutableMeanMax.builder().mean(++i).max(++i).build());
        when(diseaseProperties.timeRecoverySev()).thenReturn(ImmutableMeanMax.builder().mean(++i).max(++i).build());
        when(diseaseProperties.timeSymptomsOnset()).thenReturn(ImmutableMeanMax.builder().mean(++i).max(++i).build());
        when(diseaseProperties.timeDecline()).thenReturn(ImmutableMeanMax.builder().mean(++i).max(++i).build());
        when(diseaseProperties.timeDeath()).thenReturn(ImmutableMeanMax.builder().mean(++i).max(++i).build());

        ReflectionTestUtils.setField(eventProcessor, "diseaseProperties", diseaseProperties);

        Assert.assertEquals(diseaseProperties.timeLatent().mean(), eventProcessor.timeInCompartment(EXPOSED, ASYMPTOMATIC));
        Assert.assertEquals(diseaseProperties.timeRecoveryAsymp().mean(), eventProcessor.timeInCompartment(ASYMPTOMATIC, RECOVERED));
        Assert.assertEquals(diseaseProperties.timeLatent().mean(), eventProcessor.timeInCompartment(EXPOSED, PRESYMPTOMATIC));
        Assert.assertEquals(diseaseProperties.timeSymptomsOnset().mean(), eventProcessor.timeInCompartment(PRESYMPTOMATIC, SYMPTOMATIC));
        Assert.assertEquals(diseaseProperties.timeRecoverySymp().mean(), eventProcessor.timeInCompartment(SYMPTOMATIC, RECOVERED));
        Assert.assertEquals(diseaseProperties.timeDecline().mean(), eventProcessor.timeInCompartment(SYMPTOMATIC, SEVERELY_SYMPTOMATIC));
        Assert.assertEquals(diseaseProperties.timeRecoverySev().mean(), eventProcessor.timeInCompartment(SEVERELY_SYMPTOMATIC, RECOVERED));
        Assert.assertEquals(diseaseProperties.timeDeath().mean(), eventProcessor.timeInCompartment(SEVERELY_SYMPTOMATIC, DEAD));

    }

    @Test
    public void timeCompartmentEdge() {
        try {
            eventProcessor.timeInCompartment(SUSCEPTIBLE, RECOVERED);
        } catch (RuntimeException e) {
            Assert.assertThat(logSpy.getOutput(), containsString("Unexpected Virus statuses"));
            Assert.assertThat(logSpy.getOutput(), containsString("SUSCEPTIBLE"));
            Assert.assertThat(logSpy.getOutput(), containsString("RECOVERED"));
        }
    }

    @Test
    public void timeInStatus() {

        int time = eventProcessor.timeInStatus(NONE);
        Assert.assertEquals(0, time);

        time = eventProcessor.timeInStatus(ALERTED);
        Assert.assertEquals(1, time);

        time = eventProcessor.timeInStatus(REQUESTED_TEST);
        Assert.assertEquals(diseaseProperties.timeTestAdministered().mean(), time);

        time = eventProcessor.timeInStatus(AWAITING_RESULT);
        Assert.assertEquals(diseaseProperties.timeTestResult().mean(), time);

        time = eventProcessor.timeInStatus(TESTED_NEGATIVE);
        Assert.assertEquals(1, time);

        time = eventProcessor.timeInStatus(TESTED_POSITIVE);
        Assert.assertEquals(0, time);


    }

    @Test
    public void determineInfection() {
        Human human = mock(Human.class);
        when(human.health()).thenReturn(-1d);
        Case aCase = new Case(human);

        Map<Integer, Case> population = new HashMap<>();
        population.put(0, aCase);
        eventProcessor.setPopulation(population);
        CommonVirusEvent event = mock(CommonVirusEvent.class);
        when(event.nextStatus()).thenReturn(EXPOSED);

        VirusStatus out = eventProcessor.determineInfection(event);
        Assert.assertEquals(PRESYMPTOMATIC, out);

        when(human.health()).thenReturn(2d);
        population.put(0, aCase);
        eventProcessor.setPopulation(population);

        out = eventProcessor.determineInfection(event);
        Assert.assertEquals(ASYMPTOMATIC, out);

    }

    @Test
    public void determineSeverity() {
        Human human = mock(Human.class);
        when(human.health()).thenReturn(-1d);
        Case aCase = new Case(human);

        Map<Integer, Case> population = new HashMap<>();
        population.put(0, aCase);
        eventProcessor.setPopulation(population);
        CommonVirusEvent event = mock(CommonVirusEvent.class);
        when(event.nextStatus()).thenReturn(SYMPTOMATIC);


        VirusStatus out = eventProcessor.determineSeverity(event);
        Assert.assertEquals(SEVERELY_SYMPTOMATIC, out);

        when(human.health()).thenReturn(2d);
        population.put(0, aCase);
        eventProcessor.setPopulation(population);

        out = eventProcessor.determineSeverity(event);
        Assert.assertEquals(RECOVERED, out);
    }

    @Test
    public void determineOutcome() {
        Human human = mock(Human.class);
        when(human.health()).thenReturn(-1d);
        Case aCase = new Case(human);

        Map<Integer, Case> population = new HashMap<>();
        population.put(0, aCase);
        eventProcessor.setPopulation(population);
        CommonVirusEvent event = mock(CommonVirusEvent.class);
        when(event.nextStatus()).thenReturn(SEVERELY_SYMPTOMATIC);


        VirusStatus out = eventProcessor.determineOutcome(event);
        Assert.assertEquals(DEAD, out);

        when(human.health()).thenReturn(2d);
        population.put(0, aCase);
        eventProcessor.setPopulation(population);

        out = eventProcessor.determineOutcome(event);
        Assert.assertEquals(RECOVERED, out);
    }
}