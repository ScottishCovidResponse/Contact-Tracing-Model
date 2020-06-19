package uk.co.ramp.event;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ramp.AppConfig;
import uk.co.ramp.LogSpy;
import uk.co.ramp.Population;
import uk.co.ramp.TestConfig;
import uk.co.ramp.TestUtils;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.ImmutableAlertEvent;
import uk.co.ramp.event.types.ProcessedEventResult;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
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

    private DiseaseProperties diseaseProperties;
    private Population population;
    private DistributionSampler distributionSampler;

    private AlertEventProcessor eventProcessor;

    @Before
    public void setUp() throws Exception {
        diseaseProperties = TestUtils.diseaseProperties();
        population = mock(Population.class);
        distributionSampler = mock(DistributionSampler.class);
        when(distributionSampler.getDistributionValue(any())).thenReturn(1);
    }

    @Test
    public void timeInStatus() {
        eventProcessor = new AlertEventProcessor(population, diseaseProperties, distributionSampler);

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

        eventProcessor = new AlertEventProcessor(new Population(population), diseaseProperties, distributionSampler);

        AlertEvent event = ImmutableAlertEvent.builder().time(0).id(0).oldStatus(NONE).nextStatus(ALERTED).build();

        ProcessedEventResult eventResult = eventProcessor.processEvent(event);

        System.out.println(eventResult);

        Assert.assertEquals(1, eventResult.newAlertEvents().size());
        Assert.assertEquals(0, eventResult.newContactEvents().size());
        Assert.assertEquals(0, eventResult.newInfectionEvents().size());
        Assert.assertEquals(0, eventResult.newVirusEvents().size());
        Assert.assertEquals(1, eventResult.completedEvents().size());
        AlertEvent evnt = eventResult.newAlertEvents().get(0);

        Assert.assertEquals(1, evnt.time());
        Assert.assertEquals(0, evnt.id());
        Assert.assertEquals(ALERTED, evnt.oldStatus());
        Assert.assertEquals(REQUESTED_TEST, evnt.nextStatus());


    }

}