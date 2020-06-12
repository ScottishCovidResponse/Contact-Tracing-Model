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
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.ImmutableAlertEvent;
import uk.co.ramp.event.types.ProcessedEventResult;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.AlertStatus.*;
import static uk.co.ramp.people.AlertStatus.TESTED_POSITIVE;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;


@RunWith(SpringRunner.class)
@DirtiesContext
@Import({TestUtils.class, AppConfig.class, TestConfig.class})
public class AlertEventProcessorTest {
    @Rule
    public LogSpy logSpy = new LogSpy();

    @Autowired
    DiseaseProperties diseaseProperties;

    @Autowired
    private AlertEventProcessor eventProcessor;

    @Before
    public void setUp() throws Exception {
        diseaseProperties = TestUtils.diseaseProperties();
        ReflectionTestUtils.setField(eventProcessor, "diseaseProperties", diseaseProperties);
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
    public void runAlertEvents() {
        int infector = 0;

        Case mock0 = mock(Case.class);
        when(mock0.virusStatus()).thenReturn(SUSCEPTIBLE);
        when(mock0.isInfectious()).thenReturn(true);
        when(mock0.id()).thenReturn(infector);

        Map<Integer, Case> population = new HashMap<>();
        population.put(0, mock0);

        ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

        AlertEvent event = ImmutableAlertEvent.builder().time(0).id(0).oldStatus(NONE).nextStatus(ALERTED).eventProcessor(eventProcessor).build();

        ProcessedEventResult eventResult = eventProcessor.processEvent(event);

        System.out.println(eventResult.newEvents());

        Assert.assertEquals(1, eventResult.newEvents().size());
        AlertEvent evnt = (AlertEvent) eventResult.newEvents().get(0);

        Assert.assertEquals(1, evnt.time());
        Assert.assertEquals(0, evnt.id());
        Assert.assertEquals(ALERTED, evnt.oldStatus());
        Assert.assertEquals(REQUESTED_TEST, evnt.nextStatus());


    }

}