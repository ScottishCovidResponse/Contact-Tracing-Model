package uk.co.ramp.event;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static uk.co.ramp.people.VirusStatus.*;

@RunWith(SpringRunner.class)
@DirtiesContext
@Import({TestUtils.class, AppConfig.class, TestConfig.class})
public class EventProcessorRunnerTest {


    @Rule
    public LogSpy logSpy = new LogSpy();


    DiseaseProperties diseaseProperties;

    @Autowired
    private EventProcessorRunner eventProcessorRunner;

    @Autowired
    private EventList eventList;


    @Before
    public void setUp() throws Exception {
        diseaseProperties = TestUtils.diseaseProperties();
    }


    @Test
    public void process() {

        eventProcessorRunner = mock(EventProcessorRunner.class);
        doCallRealMethod().when(eventProcessorRunner).process(anyInt(), anyDouble(), anyInt());
        ReflectionTestUtils.setField(eventProcessorRunner, "eventList", eventList);

        eventProcessorRunner.process(0, 0.1, 1);

        verify(eventProcessorRunner, times(1)).runAllEvents(anyInt());
        verify(eventProcessorRunner, times(1)).runPolicyEvents(anyInt());
        verify(eventProcessorRunner, times(1)).createRandomInfections(anyInt(), anyDouble());

    }


    @Test
    public void createRandomInfections() {

        Map<Integer, Case> population = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            Case mock0 = mock(Case.class);
            when(mock0.virusStatus()).thenReturn(SUSCEPTIBLE);
            when(mock0.id()).thenReturn(i);
            population.put(i, mock0);
        }
        ReflectionTestUtils.setField(eventProcessorRunner, "population", new Population(population));


        List<InfectionEvent> list = eventProcessorRunner.createRandomInfections(0, 0.1);

        assertThat(list.size()).isBetween(990, 1010);
    }
}