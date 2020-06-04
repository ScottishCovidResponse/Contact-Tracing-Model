package uk.co.ramp.event;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ramp.AppConfig;
import uk.co.ramp.TestConfig;
import uk.co.ramp.TestUtils;
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.Event;
import uk.co.ramp.event.types.ImmutableVirusEvent;
import uk.co.ramp.event.types.VirusEvent;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.Human;
import uk.co.ramp.people.VirusStatus;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.*;

@RunWith(SpringRunner.class)
@Import({TestConfig.class, TestUtils.class, AppConfig.class})
public class EventProcessorTest {

    @Autowired
    RandomDataGenerator randomDataGenerator;
    private EventProcessor eventProcessor;

    @Before
    public void setUp() throws Exception {

        eventProcessor = new EventProcessor();
        eventProcessor.setRandomDataGenerator(randomDataGenerator);
    }


    @Test
    public void process() {
    }

    @Test
    public void createRandomInfections() {
    }

    @Test
    public void runPolicyEvents() {
    }

    @Test
    public void runAlertEvents() {
    }

    @Test
    public void runContactEvents() {
    }

    @Test
    public void runVirusEvents() {
    }

    @Test
    public void runInfectionEvents() {
    }

    @Test
    public void evaluateContact() {
    }

    @Test
    public void evaluateExposures() {
    }

    @Test
    public void checkForAlert() {

        VirusEvent virusEvent = ImmutableVirusEvent.builder().id(0).oldStatus(EXPOSED).newStatus(PRESYMPTOMATIC).time(1).build();

        Assert.assertTrue(eventProcessor.checkForAlert(virusEvent).isEmpty());
        int time = 1;
        int id = 0;
        virusEvent = ImmutableVirusEvent.builder().id(id).oldStatus(PRESYMPTOMATIC).newStatus(SYMPTOMATIC).time(time).build();

        Optional<Event> var = eventProcessor.checkForAlert(virusEvent);

        Assert.assertFalse(var.isEmpty());

        AlertEvent event = (AlertEvent) var.get();

        Assert.assertEquals(time + 1, event.time());
        Assert.assertEquals(AlertStatus.NONE, event.oldStatus());
        Assert.assertEquals(AlertStatus.REQUESTED_TEST, event.newStatus());
        Assert.assertEquals(id, event.id());
    }

    @Test
    public void determineNextStatus() {
    }

    @Test
    public void timeInCompartment() {
    }

    @Test
    public void timeInStatus() {
    }

    @Test
    public void getDistributionValue() {
    }

    @Test
    public void determineInfection() {
        Human human = mock(Human.class);
        when(human.health()).thenReturn(-1d);
        Case aCase = new Case(human);

        VirusStatus out = eventProcessor.determineInfection(aCase);
        Assert.assertEquals(PRESYMPTOMATIC, out);

        when(human.health()).thenReturn(2d);
        aCase = new Case(human);

        out = eventProcessor.determineInfection(aCase);
        Assert.assertEquals(ASYMPTOMATIC, out);

    }

    @Test
    public void determineSeverity() {
        Human human = mock(Human.class);
        when(human.health()).thenReturn(-1d);
        Case aCase = new Case(human);

        VirusStatus out = eventProcessor.determineSeverity(aCase);
        Assert.assertEquals(SEVERELY_SYMPTOMATIC, out);

        when(human.health()).thenReturn(2d);
        aCase = new Case(human);

        out = eventProcessor.determineSeverity(aCase);
        Assert.assertEquals(RECOVERED, out);
    }

    @Test
    public void determineOutcome() {
        Human human = mock(Human.class);
        when(human.health()).thenReturn(-1d);
        Case aCase = new Case(human);

        VirusStatus out = eventProcessor.determineOutcome(aCase);
        Assert.assertEquals(DEAD, out);

        when(human.health()).thenReturn(2d);
        aCase = new Case(human);

        out = eventProcessor.determineOutcome(aCase);
        Assert.assertEquals(RECOVERED, out);
    }
}